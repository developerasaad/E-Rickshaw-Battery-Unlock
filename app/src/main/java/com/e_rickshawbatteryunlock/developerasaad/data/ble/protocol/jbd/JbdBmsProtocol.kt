package com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.jbd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import com.e_rickshawbatteryunlock.developerasaad.core.util.BleConstants
import com.e_rickshawbatteryunlock.developerasaad.data.ble.model.GattEvent
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.BmsProtocol
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.BmsProtocolException
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "JbdBmsProtocol"

/**
 * JBD / Xiaoxiang BMS protocol implementation.
 *
 * Compatible with:
 *  - JBD (Jiabaida) BMS modules
 *  - Xiaoxiang Smart BMS
 *  - LLT Power BMS (same protocol)
 *  - Generic Chinese BMS modules advertising the FFF0 service
 *
 * Protocol documented at:
 *  - https://github.com/simat/BatteryMonitor/wiki/Generic-Chinese-Bluetooth-BMS-communication-protocol
 *  - JBD Protocol English version.pdf (FurTrader/OverkillSolarBMS)
 *
 * @param eventChannel Shared channel from [BleGattCallback]; used to await
 *        asynchronous GATT operation results.
 */
class JbdBmsProtocol(
    private val eventChannel: Channel<GattEvent>,
) : BmsProtocol {

    override val serviceUuids: List<UUID> = listOf(BleConstants.JBD_SERVICE_UUID)

    override val vendorName: String = "JBD / Xiaoxiang"

    // ─── Capability Detection ──────────────────────────────────────────────────

    override suspend fun detectCapabilities(gatt: BluetoothGatt): RecoveryCapability {
        val service = gatt.getService(BleConstants.JBD_SERVICE_UUID)
            ?: return RecoveryCapability.NONE

        val notifyChar = service.getCharacteristic(BleConstants.JBD_NOTIFY_CHAR_UUID)
        val writeChar = service.getCharacteristic(BleConstants.JBD_WRITE_CHAR_UUID)

        val canRead = notifyChar != null
        val canWrite = writeChar != null &&
            (writeChar.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0 ||
                writeChar.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0)

        return RecoveryCapability(
            canReadBatteryInfo = canRead,
            canReadCellVoltages = canRead,
            canEnableDischarge = canWrite,
            canEnableCharge = canWrite,
            canSetPassword = canWrite,
            vendorName = vendorName,
        )
    }

    // ─── Battery Info Reading ──────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override suspend fun readBatteryInfo(gatt: BluetoothGatt): BatteryInfo {
        // Step 1: Enable notifications on the notify characteristic
        enableNotifications(gatt)

        // Step 2: Request basic info (voltage, current, SoC, temp, MOS status)
        val basicFrame = sendReadCommand(gatt, BleConstants.CMD_READ_BASIC_INFO)
        val basicData = JbdFrameParser.extractData(basicFrame, BleConstants.CMD_READ_BASIC_INFO)
        val batteryInfo = JbdFrameParser.parseBasicInfo(basicData)

        // Step 3: Request cell voltages
        val cellFrame = sendReadCommand(gatt, BleConstants.CMD_READ_CELL_VOLTAGES)
        val cellData = JbdFrameParser.extractData(cellFrame, BleConstants.CMD_READ_CELL_VOLTAGES)
        val cellVoltages = JbdFrameParser.parseCellVoltages(cellData)

        return batteryInfo.copy(cellVoltagesMillivolts = cellVoltages)
    }

    // ─── Recovery (Discharge Enable) ──────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override suspend fun enableDischarge(gatt: BluetoothGatt): RecoveryResult {
        val service = gatt.getService(BleConstants.JBD_SERVICE_UUID)
            ?: return RecoveryResult.Unsupported(
                "This battery does not expose the required BLE service."
            )

        val writeChar = service.getCharacteristic(BleConstants.JBD_WRITE_CHAR_UUID)
            ?: return RecoveryResult.Unsupported(
                "This battery does not support discharge control commands."
            )

        return try {
            // Send MOS control command: enable both charge and discharge MOSFETs
            val command = JbdFrameParser.buildWriteRequest(
                command = BleConstants.CMD_WRITE_MOS_CONTROL,
                data = byteArrayOf(BleConstants.MOS_BOTH_ON),
            )

            writeCharacteristic(gatt, writeChar, command)

            // Read back the current status to verify the command was accepted
            val basicFrame = sendReadCommand(gatt, BleConstants.CMD_READ_BASIC_INFO)
            val basicData = JbdFrameParser.extractData(basicFrame, BleConstants.CMD_READ_BASIC_INFO)
            val info = JbdFrameParser.parseBasicInfo(basicData)

            if (info.isDischargeEnabled == true) {
                RecoveryResult.Success
            } else {
                RecoveryResult.Failure(
                    "Command sent but discharge switch is still OFF. " +
                        "The BMS may require a password or the battery protection may be active."
                )
            }
        } catch (e: BmsProtocolException) {
            Log.e(TAG, "Recovery failed: ${e.message}")
            RecoveryResult.Failure("Recovery failed: ${e.message ?: "Unknown BMS error"}")
        }
    }

    // ─── Password Management ───────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    override suspend fun setPassword(gatt: BluetoothGatt, password: String?): Result<Unit> {
        val service = gatt.getService(BleConstants.JBD_SERVICE_UUID)
            ?: return Result.failure(
                BmsProtocolException("BMS service not found on connected device.")
            )

        val writeChar = service.getCharacteristic(BleConstants.JBD_WRITE_CHAR_UUID)
            ?: return Result.failure(
                BmsProtocolException("Write characteristic not available on this BMS.")
            )

        return try {
            val passwordBytes = password?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()
            // Password command sub-code for JBD: 0x19 (set password register)
            // Data format: [0x01, password_length, ...password_bytes]
            // If password is null/empty, send [0x01, 0x00] to clear the password.
            val data = if (passwordBytes.isEmpty()) {
                byteArrayOf(0x01, 0x00)
            } else {
                byteArrayOf(0x01, passwordBytes.size.toByte()) + passwordBytes
            }

            val command = JbdFrameParser.buildWriteRequest(
                command = 0x19.toByte(),
                data = data,
            )

            writeCharacteristic(gatt, writeChar, command)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BmsProtocolException("Password operation failed: ${e.message}", e))
        }
    }

    // ─── Internal GATT Operations ─────────────────────────────────────────────

    /**
     * Enables BLE notifications on the JBD notify characteristic.
     *
     * Must be called before sending any read command, as responses arrive
     * asynchronously via notifications rather than as direct read responses.
     *
     * ## CCCD Descriptor lookup strategy
     *
     * Many JBD clones and Xiaoxiang modules don't expose the CCCD via the standard
     * UUID `00002902-0000-1000-8000-00805f9b34fb` through [getDescriptor]. This
     * happens because:
     *  1. Some firmwares only have one descriptor and don't properly register its UUID.
     *  2. Android's [getDescriptor] returns null when the UUID doesn't match exactly.
     *
     * Fix: fall back to iterating [BluetoothGattCharacteristic.descriptors] and picking
     * the first descriptor if the direct UUID lookup fails.
     */
    @SuppressLint("MissingPermission")
    private suspend fun enableNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(BleConstants.JBD_SERVICE_UUID)
            ?: throw BmsProtocolException("JBD service not found")

        val characteristic = service.getCharacteristic(BleConstants.JBD_NOTIFY_CHAR_UUID)
            ?: throw BmsProtocolException("JBD notify characteristic not found")

        val enabled = gatt.setCharacteristicNotification(characteristic, true)
        if (!enabled) {
            throw BmsProtocolException("Failed to enable local characteristic notifications")
        }

        // FIX: Many JBD clones don't expose CCCD by standard UUID — fall back to
        // first descriptor. If the characteristic has no descriptors at all, skip
        // the CCCD write (setCharacteristicNotification alone may be sufficient on
        // some devices; we'll discover this from whether responses arrive).
        val descriptor: BluetoothGattDescriptor? =
            characteristic.getDescriptor(BleConstants.CCCD_UUID)
                ?: characteristic.descriptors.firstOrNull().also { fallback ->
                    if (fallback != null) {
                        Log.w(TAG, "CCCD not found by UUID — using fallback descriptor " +
                            "${fallback.uuid} on ${characteristic.uuid}")
                    }
                }

        if (descriptor == null) {
            // No descriptor at all — some very stripped-down BMS firmware relies solely
            // on setCharacteristicNotification without a CCCD write. Log and continue.
            Log.w(TAG, "No CCCD descriptor found on notify characteristic — " +
                "proceeding without descriptor write. Notifications may not arrive.")
            return
        }

        withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)

            // Drain the channel until we get the descriptor write confirmation.
            // Accept any descriptor UUID (not just CCCD_UUID) because the fallback
            // descriptor may have a different UUID.
            for (event in eventChannel) {
                if (event is GattEvent.DescriptorWrite) {
                    if (event.status != BluetoothGatt.GATT_SUCCESS) {
                        throw BmsProtocolException(
                            "CCCD write failed with status ${event.status}"
                        )
                    }
                    break
                }
            }
        }
    }

    /**
     * Sends a read command to the BMS and waits for the notification response.
     *
     * @param gatt    The active GATT connection.
     * @param command The command byte to send (e.g., [BleConstants.CMD_READ_BASIC_INFO]).
     * @return The raw response frame bytes.
     */
    @SuppressLint("MissingPermission")
    private suspend fun sendReadCommand(gatt: BluetoothGatt, command: Byte): ByteArray {
        val service = gatt.getService(BleConstants.JBD_SERVICE_UUID)
            ?: throw BmsProtocolException("JBD service not found")

        val writeChar = service.getCharacteristic(BleConstants.JBD_WRITE_CHAR_UUID)
            ?: throw BmsProtocolException("JBD write characteristic not found")

        val request = JbdFrameParser.buildReadRequest(command)

        return withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
            @Suppress("DEPRECATION")
            writeChar.value = request
            // Read commands use NO_RESPONSE — the BMS response arrives on the notify
            // characteristic, not as a write ACK. Using WRITE_TYPE_DEFAULT here would
            // cause GATT status 3 (WRITE_NOT_PERMITTED) on BMS modules that only
            // advertise WRITE_NO_RESPONSE property on the write characteristic.
            writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            val written = gatt.writeCharacteristic(writeChar)
            if (!written) {
                throw BmsProtocolException("Failed to initiate write for command 0x${"%02X".format(command)}")
            }

            // Accumulate notification bytes until we have a complete frame (ends with 0x77)
            // JBD BMS can fragment large responses across multiple notifications
            val buffer = mutableListOf<Byte>()
            var frameComplete = false

            for (event in eventChannel) {
                if (event is GattEvent.CharacteristicChanged &&
                    event.uuid == BleConstants.JBD_NOTIFY_CHAR_UUID
                ) {
                    buffer.addAll(event.value.toList())
                    // A complete frame ends with 0x77 and has the start byte at position 0
                    if (buffer.size >= 7 &&
                        buffer[0] == BleConstants.FRAME_START &&
                        buffer.last() == BleConstants.FRAME_END
                    ) {
                        frameComplete = true
                        break
                    }
                }
            }

            if (!frameComplete) {
                throw BmsProtocolException("Incomplete frame received for command 0x${"%02X".format(command)}")
            }

            buffer.toByteArray()
        }
    }

    /**
     * Writes a command byte array to the BMS write characteristic.
     *
     * ## Write type selection
     *
     * FIX (was causing GATT status 3 = GATT_WRITE_NOT_PERMITTED):
     * The JBD write characteristic (`FF02`) only declares WRITE_NO_RESPONSE property.
     * Using WRITE_TYPE_DEFAULT requires the BMS to send an ATT Write Response, which
     * it never does — Android's GATT stack returns status 3 (not permitted) because
     * the characteristic doesn't have the PROPERTY_WRITE bit set.
     *
     * Solution: detect from the characteristic's declared properties which write type
     * to use. Fall back to NO_RESPONSE if PROPERTY_WRITE is absent.
     */
    @SuppressLint("MissingPermission")
    private suspend fun writeCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
    ) {
        // Determine the correct write type from the characteristic's declared properties.
        // PROPERTY_WRITE (0x08) = requires ATT Write Response (WRITE_TYPE_DEFAULT)
        // PROPERTY_WRITE_NO_RESPONSE (0x04) = fire-and-forget (WRITE_TYPE_NO_RESPONSE)
        // JBD FF02 only has PROPERTY_WRITE_NO_RESPONSE → always use NO_RESPONSE.
        val writeType = if (
            characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
        ) {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        }

        withTimeout(BleConstants.GATT_OPERATION_TIMEOUT_MS) {
            @Suppress("DEPRECATION")
            characteristic.value = data
            characteristic.writeType = writeType
            val initiated = gatt.writeCharacteristic(characteristic)
            if (!initiated) {
                throw BmsProtocolException("Failed to initiate characteristic write")
            }

            if (writeType == BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) {
                // Only wait for a write ACK when the BMS is expected to send one.
                for (event in eventChannel) {
                    if (event is GattEvent.CharacteristicWrite &&
                        event.uuid == characteristic.uuid
                    ) {
                        if (event.status != BluetoothGatt.GATT_SUCCESS) {
                            throw BmsProtocolException(
                                "Write failed with GATT status ${event.status}"
                            )
                        }
                        break
                    }
                }
            }
            // WRITE_TYPE_NO_RESPONSE: no ACK comes — the BMS may send a notification
            // asynchronously; the caller is responsible for reading it if needed.
        }
    }
}
