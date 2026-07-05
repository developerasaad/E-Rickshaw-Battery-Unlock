package com.e_rickshawbatteryunlock.developerasaad.domain.repository

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import kotlinx.coroutines.flow.Flow

/**
 * Defines all BLE-related operations as an abstraction over the hardware.
 *
 * Implementations live in the data layer ([BatteryRepositoryImpl]) and delegate
 * to [BleScanner] and [BleManager]. Presentation and domain layers never touch
 * Android Bluetooth types directly.
 */
interface BatteryRepository {

    /**
     * Starts a BLE scan and emits updated lists of discovered compatible batteries.
     *
     * The flow remains active until the caller's coroutine scope is cancelled,
     * at which point scanning stops and BLE scanner resources are released.
     *
     * Emissions are deduplicated by MAC address; RSSI updates cause re-emission
     * of the full updated list.
     */
    fun scanForBatteries(): Flow<List<BatteryDevice>>

    /**
     * Establishes a GATT connection to the specified [device], discovers services,
     * and detects the available protocol capabilities.
     *
     * @return [Result.success] if connected and services discovered successfully.
     *         [Result.failure] with a descriptive exception on any error.
     */
    suspend fun connect(device: BatteryDevice): Result<Unit>

    /**
     * Observes the current BLE connection lifecycle state.
     *
     * Always emits [ConnectionState.DISCONNECTED] as the initial value.
     */
    fun observeConnectionState(): Flow<ConnectionState>

    /**
     * Observes live battery telemetry from the connected BMS.
     *
     * Emits [BatteryInfo.EMPTY] until the first data frame is received.
     * Continues emitting as new notification packets arrive from the BMS.
     */
    fun observeBatteryInfo(): Flow<BatteryInfo>

    /**
     * Returns the capabilities detected for the currently connected BMS.
     *
     * Must only be called after a successful [connect]; calling it while
     * disconnected returns [RecoveryCapability.NONE].
     */
    suspend fun getRecoveryCapability(): RecoveryCapability

    /**
     * Sends the discharge-enable command to the connected BMS.
     *
     * Performs a capability check before attempting the write. Returns
     * [RecoveryResult.Unsupported] if the BMS does not expose the required
     * write characteristic.
     */
    suspend fun executeRecovery(): RecoveryResult

    /**
     * Sets, updates, or removes the password on the connected BMS.
     *
     * @param password The new password string, or null to remove the password.
     * @return [Result.success] on success, [Result.failure] with a descriptive message on error.
     */
    suspend fun setPassword(password: String?): Result<Unit>

    /**
     * Cleanly disconnects from the current BLE device and releases all GATT resources.
     *
     * Safe to call even if already disconnected.
     */
    suspend fun disconnect()
}
