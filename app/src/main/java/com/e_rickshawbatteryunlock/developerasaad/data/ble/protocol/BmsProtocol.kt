package com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol

import android.bluetooth.BluetoothGatt
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import java.util.UUID

/**
 * Protocol abstraction for vendor-specific BMS communication.
 *
 * Each BMS vendor (JBD, DALY, ANT, PACE, etc.) implements this interface
 * independently. The [BmsProtocolFactory] selects the correct implementation
 * at runtime based on discovered GATT services.
 *
 * Implementations must be stateless — all state is carried by [BluetoothGatt].
 * This ensures a fresh protocol instance can be created without side effects.
 */
interface BmsProtocol {

    /**
     * The GATT service UUIDs that identify this protocol.
     *
     * The factory uses these UUIDs to match discovered services against
     * registered protocol implementations.
     */
    val serviceUuids: List<UUID>

    /**
     * Human-readable vendor name displayed in the app for diagnostics.
     * Example: "JBD / Xiaoxiang", "DALY BMS".
     */
    val vendorName: String

    /**
     * Inspects the discovered GATT services and characteristics to determine
     * which operations this specific device instance supports.
     *
     * This is called once after service discovery completes. The result is
     * cached by [BleManager] and used to gate UI actions.
     */
    suspend fun detectCapabilities(gatt: BluetoothGatt): RecoveryCapability

    /**
     * Reads a fresh telemetry snapshot from the BMS.
     *
     * Sends the appropriate read commands and waits for the response
     * notification before returning. Should not block for more than
     * [BleConstants.GATT_OPERATION_TIMEOUT_MS].
     *
     * @throws BmsProtocolException if the BMS returns an error frame or
     *         the characteristic is missing.
     */
    suspend fun readBatteryInfo(gatt: BluetoothGatt): BatteryInfo

    /**
     * Sends the discharge-enable (unlock) command to the BMS.
     *
     * @return [RecoveryResult.Success] if the command was accepted.
     *         [RecoveryResult.Failure] if the BMS rejected the command.
     *         [RecoveryResult.Unsupported] if the required characteristic is absent.
     */
    suspend fun enableDischarge(gatt: BluetoothGatt): RecoveryResult

    /**
     * Sets or removes the password on the BMS.
     *
     * @param password The new password, or null to remove.
     * @return [Result.success] on acceptance, [Result.failure] with reason on rejection.
     */
    suspend fun setPassword(gatt: BluetoothGatt, password: String?): Result<Unit>
}

/**
 * Thrown when the BMS returns a protocol-level error frame or when parsing
 * of a response frame fails.
 */
class BmsProtocolException(message: String, cause: Throwable? = null) : Exception(message, cause)
