package com.e_rickshawbatteryunlock.developerasaad.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

private const val TAG = "BleScanner"

/**
 * Wraps the Android BLE scanner API in a [Flow]-based interface.
 *
 * The scan starts when the [Flow] is collected and stops automatically when
 * the collecting coroutine is cancelled — preventing resource leaks even if
 * the app is backgrounded.
 *
 * The returned [Flow] emits an updated map of [BatteryDevice] objects keyed by
 * MAC address so that RSSI updates to existing devices don't create duplicates.
 *
 * Permission requirements:
 *  - API 21–30: [android.Manifest.permission.ACCESS_FINE_LOCATION]
 *  - API 31+:   [android.Manifest.permission.BLUETOOTH_SCAN]
 *
 * The caller (ViewModel / use case) is responsible for verifying permissions
 * before calling [scan]. If called without permission, the flow will emit an
 * error via [ScanCallback.onScanFailed].
 */
class BleScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
) {

    /**
     * Starts a BLE scan and emits continuously updated lists of discovered devices.
     *
     * Uses [ScanSettings.SCAN_MODE_LOW_LATENCY] for rapid device discovery.
     * The scan applies no [ScanFilter] so that all nearby BLE devices are visible;
     * the user selects the correct device by name/address.
     *
     * @return A [Flow] emitting updated [List]s of [BatteryDevice]. Each emission
     *         is the full current set of discovered devices (deduplicated by address).
     */
    @SuppressLint("MissingPermission")
    fun scan(): Flow<List<BatteryDevice>> = callbackFlow {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BluetoothLeScanner is null — Bluetooth may be disabled")
            close(IllegalStateException("Bluetooth is not enabled. Please enable Bluetooth and try again."))
            return@callbackFlow
        }

        // Map of address → device for deduplication and RSSI updates
        val discoveredDevices = mutableMapOf<String, BatteryDevice>()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = BatteryDevice(
                    name = result.device.name,
                    address = result.device.address,
                    rssi = result.rssi,
                )
                discoveredDevices[device.address] = device
                trySend(discoveredDevices.values.toList())
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { result ->
                    val device = BatteryDevice(
                        name = result.device.name,
                        address = result.device.address,
                        rssi = result.rssi,
                    )
                    discoveredDevices[device.address] = device
                }
                trySend(discoveredDevices.values.toList())
            }

            override fun onScanFailed(errorCode: Int) {
                val reason = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Scan already in progress"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scanning not supported on this device"
                    SCAN_FAILED_INTERNAL_ERROR -> "Internal BLE scan error"
                    else -> "Unknown scan error (code $errorCode)"
                }
                Log.e(TAG, "BLE scan failed: $reason")
                close(IllegalStateException(reason))
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        Log.d(TAG, "Starting BLE scan")
        scanner.startScan(null, settings, scanCallback)

        // When the Flow collector cancels, stop the scan and release resources
        awaitClose {
            Log.d(TAG, "Stopping BLE scan")
            try {
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping scan: ${e.message}")
            }
        }
    }.conflate() // Drop intermediate emissions if the collector is slow
}
