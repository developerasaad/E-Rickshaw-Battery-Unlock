package com.e_rickshawbatteryunlock.developerasaad.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.e_rickshawbatteryunlock.developerasaad.core.util.BleConstants
import com.e_rickshawbatteryunlock.developerasaad.data.ble.model.GattEvent
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.BmsProtocol
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.BmsProtocolFactory
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BleManager"

/**
 * Central manager for the BLE GATT connection lifecycle.
 *
 * Responsibilities:
 *  - Establishing and maintaining a single [BluetoothGatt] connection
 *  - Service and characteristic discovery
 *  - Protocol detection via [BmsProtocolFactory]
 *  - Serializing all GATT operations via [operationMutex] (required by Android GATT API)
 *  - Exposing connection state and battery info as [StateFlow]
 *  - MTU negotiation
 *  - Reconnect with exponential back-off (up to [BleConstants.MAX_RECONNECT_ATTEMPTS])
 *  - Clean resource disposal on disconnect
 *
 * Thread safety: All GATT operations go through [operationMutex]. The [eventChannel]
 * is consumed exclusively within the locked sections, preventing race conditions.
 *
 * Lifecycle: This is a [Singleton] because only one BLE device can be managed at
 * a time. It is NOT tied to an Activity or ViewModel lifecycle.
 */
@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val protocolFactory: BmsProtocolFactory,
    /** Shared singleton channel — same instance used by [BleGattCallback] and protocol impls. */
    val eventChannel: Channel<GattEvent>,
) {

    // ─── State ─────────────────────────────────────────────────────────────────

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo.EMPTY)
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private var activeGatt: BluetoothGatt? = null
    private var activeProtocol: BmsProtocol? = null
    private var detectedCapability: RecoveryCapability = RecoveryCapability.NONE

    /** Serializes all GATT operations — Android GATT is not thread-safe. */
    private val operationMutex = Mutex()

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    // ─── Connection ────────────────────────────────────────────────────────────

    /**
     * Connects to the BLE device at [address] with automatic reconnect.
     *
     * Retries up to [BleConstants.MAX_RECONNECT_ATTEMPTS] times with exponential
     * back-off before giving up and emitting [ConnectionState.FAILED].
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(address: String): Result<Unit> = operationMutex.withLock {
        var attempt = 0
        var lastError: Exception? = null

        while (attempt < BleConstants.MAX_RECONNECT_ATTEMPTS) {
            try {
                if (attempt > 0) {
                    val delay = BleConstants.RECONNECT_BASE_DELAY_MS * (1L shl (attempt - 1))
                    Log.d(TAG, "Reconnect attempt $attempt, waiting ${delay}ms")
                    delay(delay)
                }

                val result = attemptConnect(address)
                if (result.isSuccess) return@withLock result
                lastError = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastError = e
            }
            attempt++
        }

        _connectionState.value = ConnectionState.FAILED
        Result.failure(lastError ?: Exception("Connection failed after $attempt attempts"))
    }

    @SuppressLint("MissingPermission")
    private suspend fun attemptConnect(address: String): Result<Unit> {
        _connectionState.value = ConnectionState.CONNECTING
        // Drain stale events from a previous connection
        while (eventChannel.tryReceive().isSuccess) { /* drain */ }

        val bluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
        val gattCallback = BleGattCallback(eventChannel)

        Log.d(TAG, "Connecting to $address")
        val gatt = bluetoothDevice.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            ?: return Result.failure(Exception("connectGatt returned null"))

        return try {
            // Wait for connection
            withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
                for (event in eventChannel) {
                    if (event is GattEvent.ConnectionStateChanged) {
                        if (event.newState == BluetoothProfile.STATE_CONNECTED &&
                            event.status == BluetoothGatt.GATT_SUCCESS
                        ) break
                        if (event.status != BluetoothGatt.GATT_SUCCESS) {
                            throw Exception("GATT connection failed with status ${event.status}")
                        }
                    }
                }
            }

            // Negotiate MTU for larger frames
            gatt.requestMtu(BleConstants.REQUESTED_MTU)
            withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
                for (event in eventChannel) {
                    if (event is GattEvent.MtuChanged) break
                }
            }

            // Clear Android GATT cache before discovery.
            // Android caches the remote GATT service map across connections. A stale
            // cache can omit descriptors (CCCD, etc.) that physically exist on the device.
            // BluetoothGatt.refresh() is a hidden API that clears this cache and forces
            // a fresh ATT service discovery. Wrapped in try/catch — it may not exist on
            // all Android builds but works on AOSP 5.0+ and all tested vendor ROMs.
            refreshGattCache(gatt)

            // Discover services
            _connectionState.value = ConnectionState.DISCOVERING_SERVICES
            Log.d(TAG, "Connected, discovering services")
            gatt.discoverServices()


            withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
                for (event in eventChannel) {
                    if (event is GattEvent.ServicesDiscovered) {
                        if (event.status != BluetoothGatt.GATT_SUCCESS) {
                            throw Exception("Service discovery failed with status ${event.status}")
                        }
                        break
                    }
                }
            }

            // Detect protocol
            val protocol = protocolFactory.detectProtocol(gatt)
            if (protocol == null) {
                gatt.disconnect()
                gatt.close()
                return Result.failure(
                    Exception(
                        "No compatible BMS protocol detected. " +
                            "This device may not be a supported battery."
                    )
                )
            }

            activeGatt = gatt
            activeProtocol = protocol
            detectedCapability = protocol.detectCapabilities(gatt)
            _connectionState.value = ConnectionState.CONNECTED

            Log.d(TAG, "Connected to ${protocol.vendorName} BMS")

            // Start polling for battery info every 5 seconds
            startPolling()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Connection attempt failed: ${e.message}")
            gatt.disconnect()
            gatt.close()
            Result.failure(e)
        }
    }

    // ─── Polling ───────────────────────────────────────────────────────────────

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = managerScope.launch {
            while (true) {
                delay(5_000L)
                if (_connectionState.value != ConnectionState.CONNECTED) break
                try {
                    val gatt = activeGatt ?: break
                    val protocol = activeProtocol ?: break
                    val info = protocol.readBatteryInfo(gatt)
                    _batteryInfo.value = info
                } catch (e: Exception) {
                    Log.w(TAG, "Polling failed: ${e.message}")
                }
            }
        }
    }

    // ─── Operations ────────────────────────────────────────────────────────────

    /**
     * Returns the detected capabilities for the currently connected device.
     * Returns [RecoveryCapability.NONE] if not connected.
     */
    fun getCapability(): RecoveryCapability = detectedCapability

    /**
     * Reads fresh battery telemetry on demand and updates [batteryInfo].
     */
    suspend fun refreshBatteryInfo(): Result<BatteryInfo> = operationMutex.withLock {
        val gatt = activeGatt
            ?: return Result.failure(IllegalStateException("Not connected"))
        val protocol = activeProtocol
            ?: return Result.failure(IllegalStateException("No protocol active"))
        return try {
            val info = protocol.readBatteryInfo(gatt)
            _batteryInfo.value = info
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends the discharge-enable recovery command.
     */
    suspend fun executeRecovery(): RecoveryResult = operationMutex.withLock {
        val gatt = activeGatt
            ?: return RecoveryResult.Failure("Not connected to a battery")
        val protocol = activeProtocol
            ?: return RecoveryResult.Failure("No BMS protocol detected")
        return protocol.enableDischarge(gatt)
    }

    /**
     * Sets or removes the password on the connected BMS.
     */
    suspend fun setPassword(password: String?): Result<Unit> = operationMutex.withLock {
        val gatt = activeGatt
            ?: return Result.failure(IllegalStateException("Not connected"))
        val protocol = activeProtocol
            ?: return Result.failure(IllegalStateException("No protocol active"))
        return protocol.setPassword(gatt, password)
    }

    // ─── Disconnect ────────────────────────────────────────────────────────────

    /**
     * Disconnects from the current BLE device and releases all GATT resources.
     *
     * Safe to call even if already disconnected.
     */
    @SuppressLint("MissingPermission")
    suspend fun disconnect(): Unit = operationMutex.withLock {
        pollingJob?.cancel()
        pollingJob = null
        activeGatt?.let { gatt ->
            try {
                gatt.disconnect()
                gatt.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error during disconnect: ${e.message}")
            }
        }
        activeGatt = null
        activeProtocol = null
        detectedCapability = RecoveryCapability.NONE
        _connectionState.value = ConnectionState.DISCONNECTED
        _batteryInfo.value = BatteryInfo.EMPTY
        Log.d(TAG, "Disconnected and resources released")
        Unit  // explicit Unit — Log.d() returns Int; this ensures withLock infers Unit
    }

    /** Exposes connection state as a [Flow] for repository consumers. */
    fun observeConnectionState(): Flow<ConnectionState> = connectionState

    /** Exposes battery info as a [Flow] for repository consumers. */
    fun observeBatteryInfo(): Flow<BatteryInfo> = batteryInfo

    // ─── GATT Cache ────────────────────────────────────────────────────────────

    /**
     * Clears Android's GATT service cache using the hidden [BluetoothGatt.refresh] method.
     *
     * **Why this is necessary:**
     * Android caches the remote device's ATT service/characteristic/descriptor map after
     * the first successful connection. On subsequent connections to the same device, it
     * returns the cached map instead of re-discovering from the hardware. If the cache
     * was built when the device was in a different state (e.g., no CCCD visible), the
     * CCCD descriptor will be permanently absent from [BluetoothGattCharacteristic.descriptors]
     * even though it physically exists on the BMS board.
     *
     * Calling this forces a full ATT discovery on the next [BluetoothGatt.discoverServices]
     * call, picking up all descriptors correctly.
     *
     * `refresh()` is a `@hide` method present on AOSP since Android 5.0 and on all major
     * vendor ROMs (Samsung, Xiaomi, OnePlus, etc.). It may be removed in future Android
     * versions; the try/catch ensures we fail gracefully rather than crashing.
     */
    private fun refreshGattCache(gatt: BluetoothGatt) {
        try {
            val refresh = gatt.javaClass.getMethod("refresh")
            val result = refresh.invoke(gatt) as Boolean
            Log.d(TAG, "GATT cache refresh: $result")
        } catch (e: Exception) {
            // Safe to ignore — discoverServices will still work, just may use cached data
            Log.w(TAG, "Could not refresh GATT cache (hidden API unavailable): ${e.message}")
        }
    }
}
