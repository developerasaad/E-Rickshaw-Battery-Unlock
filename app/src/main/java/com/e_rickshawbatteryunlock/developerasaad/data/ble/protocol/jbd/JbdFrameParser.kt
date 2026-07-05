package com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.jbd

import com.e_rickshawbatteryunlock.developerasaad.core.extension.jbdChecksum
import com.e_rickshawbatteryunlock.developerasaad.core.extension.readSignedInt16BE
import com.e_rickshawbatteryunlock.developerasaad.core.extension.readUInt16BE
import com.e_rickshawbatteryunlock.developerasaad.core.extension.toHexString
import com.e_rickshawbatteryunlock.developerasaad.core.extension.toUnsignedInt
import com.e_rickshawbatteryunlock.developerasaad.core.util.BleConstants
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.BmsProtocolException
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryStatus

/**
 * Parses JBD / Xiaoxiang BMS protocol response frames into domain models.
 *
 * Frame structure:
 *   [0]      0xDD        Start byte
 *   [1]      cmd         Command echo (matches request command)
 *   [2]      status      0x00 = OK, 0x80 = Error
 *   [3]      len         Length of data field in bytes
 *   [4..N-3] data        Response data (len bytes)
 *   [N-2]    chk_hi      Checksum high byte
 *   [N-1]    chk_lo      Checksum low byte
 *   [N]      0x77        End byte
 *
 * Reference: simat/BatteryMonitor Wiki, JBD Protocol English version PDF
 */
internal object JbdFrameParser {

    /**
     * Validates and extracts the data section from a complete JBD response frame.
     *
     * @param frame The raw byte array received from the BMS notify characteristic.
     * @param expectedCommand The command code expected in byte[1].
     * @return The data bytes (between the length byte and checksum).
     * @throws BmsProtocolException if the frame is malformed, too short, has a bad
     *         start/end byte, wrong command, error status, or checksum mismatch.
     */
    fun extractData(frame: ByteArray, expectedCommand: Byte): ByteArray {
        if (frame.size < 7) {
            throw BmsProtocolException(
                "Frame too short: ${frame.size} bytes (minimum 7 expected). " +
                    "Raw: ${frame.toHexString()}"
            )
        }

        if (frame[0] != BleConstants.FRAME_START) {
            throw BmsProtocolException(
                "Invalid start byte: 0x${"%02X".format(frame[0])} (expected 0xDD)"
            )
        }

        if (frame[frame.size - 1] != BleConstants.FRAME_END) {
            throw BmsProtocolException(
                "Invalid end byte: 0x${"%02X".format(frame[frame.size - 1])} (expected 0x77)"
            )
        }

        val cmd = frame[1]
        if (cmd != expectedCommand) {
            throw BmsProtocolException(
                "Command mismatch: got 0x${"%02X".format(cmd)}, " +
                    "expected 0x${"%02X".format(expectedCommand)}"
            )
        }

        val statusByte = frame[2]
        if (statusByte == BleConstants.FRAME_RESPONSE_ERROR) {
            val errorCode = if (frame.size > 4) frame[4].toUnsignedInt() else -1
            throw BmsProtocolException(
                "BMS returned error frame for command 0x${"%02X".format(cmd)}, " +
                    "error code: 0x${"%02X".format(errorCode)}"
            )
        }

        val dataLength = frame[3].toUnsignedInt()
        val expectedFrameLength = 4 + dataLength + 3 // header(4) + data + checksum(2) + end(1)
        if (frame.size < expectedFrameLength) {
            throw BmsProtocolException(
                "Frame length mismatch: declared $dataLength data bytes but frame is " +
                    "${frame.size} bytes (expected $expectedFrameLength)"
            )
        }

        val data = frame.copyOfRange(4, 4 + dataLength)

        // Validate checksum: checksum covers cmd, status, len, and data bytes
        val checksumData = frame.copyOfRange(1, 4 + dataLength)
        val expectedChecksum = checksumData.jbdChecksum()
        val actualChecksumHi = frame[4 + dataLength]
        val actualChecksumLo = frame[4 + dataLength + 1]
        if (actualChecksumHi != expectedChecksum[0] || actualChecksumLo != expectedChecksum[1]) {
            throw BmsProtocolException(
                "Checksum mismatch: expected ${expectedChecksum.toHexString()}, " +
                    "got ${byteArrayOf(actualChecksumHi, actualChecksumLo).toHexString()}"
            )
        }

        return data
    }

    /**
     * Builds a JBD request frame for a read command.
     *
     * Frame format: DD A5 <cmd> 00 <chk_hi> <chk_lo> 77
     * Checksum covers: [cmd, 00] (the bytes after 0xA5 up to checksum)
     */
    fun buildReadRequest(command: Byte): ByteArray {
        val checksumData = byteArrayOf(command, 0x00)
        val checksum = checksumData.jbdChecksum()
        return byteArrayOf(
            BleConstants.FRAME_START,
            BleConstants.FRAME_REQUEST,
            command,
            0x00,
            checksum[0],
            checksum[1],
            BleConstants.FRAME_END,
        )
    }

    /**
     * Builds a JBD write request frame for MOS control.
     *
     * Write frame format: DD A5 <cmd> <len> <data...> <chk_hi> <chk_lo> 77
     *
     * @param command The command byte (e.g., [BleConstants.CMD_WRITE_MOS_CONTROL]).
     * @param data    The data payload to send.
     */
    fun buildWriteRequest(command: Byte, data: ByteArray): ByteArray {
        val len = data.size.toByte()
        val checksumData = byteArrayOf(command, len) + data
        val checksum = checksumData.jbdChecksum()
        return byteArrayOf(BleConstants.FRAME_START, BleConstants.FRAME_REQUEST, command, len) +
            data +
            byteArrayOf(checksum[0], checksum[1], BleConstants.FRAME_END)
    }

    /**
     * Parses the data section of a basic information response (command 0x03) into
     * a [BatteryInfo] domain model.
     *
     * @param data The raw data bytes extracted by [extractData].
     */
    fun parseBasicInfo(data: ByteArray): BatteryInfo {
        if (data.size < BleConstants.BASIC_OFFSET_TEMP_DATA) {
            throw BmsProtocolException(
                "Basic info data too short: ${data.size} bytes " +
                    "(minimum ${BleConstants.BASIC_OFFSET_TEMP_DATA} required)"
            )
        }

        // Voltage: bytes 0–1, unit = 10mV, multiply by 10 for mV
        val packVoltageMillivolts = data.readUInt16BE(BleConstants.BASIC_OFFSET_PACK_VOLTAGE) * 10

        // Current: bytes 2–3, unit = 10mA, signed (positive = charging)
        val currentMilliamps = data.readSignedInt16BE(BleConstants.BASIC_OFFSET_CURRENT) * 10

        // Remaining capacity: bytes 4–5, unit = 10mAh
        val remainingCapacityMAh = data.readUInt16BE(BleConstants.BASIC_OFFSET_REMAINING_CAP) * 10

        // Full capacity: bytes 6–7, unit = 10mAh
        val fullCapacityMAh = data.readUInt16BE(BleConstants.BASIC_OFFSET_FULL_CAP) * 10

        // Cycle count: bytes 8–9
        val cycleCount = data.readUInt16BE(BleConstants.BASIC_OFFSET_CYCLE_COUNT)

        // Balance bitmask: bytes 12–13 (low) and 14–15 (high) combined into 32-bit
        val balanceLow = data.readUInt16BE(BleConstants.BASIC_OFFSET_BALANCE_LOW)
        val balanceHigh = if (data.size > BleConstants.BASIC_OFFSET_BALANCE_HIGH + 1) {
            data.readUInt16BE(BleConstants.BASIC_OFFSET_BALANCE_HIGH)
        } else {
            0
        }
        val balancingCells = (balanceHigh shl 16) or balanceLow

        // Protection status: bytes 16–17
        val protectionStatus = data.readUInt16BE(BleConstants.BASIC_OFFSET_PROTECTION_STATUS)
        val batteryStatus = parseProtectionStatus(protectionStatus)

        // State of charge: byte 19
        val stateOfChargePercent = data[BleConstants.BASIC_OFFSET_SOC].toUnsignedInt()

        // MOS status: byte 20 (bit 0 = charge MOS, bit 1 = discharge MOS)
        val mosStatus = data[BleConstants.BASIC_OFFSET_MOS_STATUS].toUnsignedInt()
        val isChargingEnabled = (mosStatus and 0x01) != 0
        val isDischargeEnabled = (mosStatus and 0x02) != 0

        // Temperature count: byte 22
        val tempCount = if (data.size > BleConstants.BASIC_OFFSET_TEMP_COUNT) {
            data[BleConstants.BASIC_OFFSET_TEMP_COUNT].toUnsignedInt()
        } else {
            0
        }

        // Temperature data: byte 23+, 2 bytes each, unit = 0.1K, subtract 2731 for 0.1°C
        val temperatures = (0 until tempCount).mapNotNull { i ->
            val offset = BleConstants.BASIC_OFFSET_TEMP_DATA + (i * 2)
            if (offset + 1 < data.size) {
                val rawTemp = data.readUInt16BE(offset)
                // JBD temperature is in 0.1K — subtract 273.1 * 10 = 2731 for Celsius tenths
                rawTemp - 2731
            } else {
                null
            }
        }

        return BatteryInfo(
            packVoltageMillivolts = packVoltageMillivolts,
            currentMilliamps = currentMilliamps,
            stateOfChargePercent = stateOfChargePercent,
            remainingCapacityMAh = remainingCapacityMAh,
            fullCapacityMAh = fullCapacityMAh,
            cycleCount = cycleCount,
            temperatures = temperatures,
            cellVoltagesMillivolts = emptyList(), // populated by parseCellVoltages
            status = batteryStatus,
            isChargingEnabled = isChargingEnabled,
            isDischargeEnabled = isDischargeEnabled,
            balancingCells = balancingCells,
        )
    }

    /**
     * Parses the data section of a cell voltage response (command 0x04).
     *
     * @param data The raw data bytes extracted by [extractData].
     * @return List of cell voltages in millivolts.
     */
    fun parseCellVoltages(data: ByteArray): List<Int> {
        val cellCount = data.size / 2
        return (0 until cellCount).map { i ->
            data.readUInt16BE(i * 2)
        }
    }

    /**
     * Maps the JBD protection status register bitmask to a [BatteryStatus] enum.
     *
     * Protection register bit definitions (from JBD protocol):
     *  Bit 0:  Cell over-voltage
     *  Bit 1:  Cell under-voltage
     *  Bit 2:  Pack over-voltage
     *  Bit 3:  Pack under-voltage
     *  Bit 4:  Charge over-temperature
     *  Bit 5:  Charge under-temperature
     *  Bit 6:  Discharge over-temperature
     *  Bit 8:  Charge over-current
     *  Bit 9:  Discharge over-current
     *  Bit 10: Short circuit
     */
    private fun parseProtectionStatus(protectionBits: Int): BatteryStatus = when {
        protectionBits == 0 -> BatteryStatus.NORMAL
        protectionBits and 0x0004 != 0 -> BatteryStatus.OVER_VOLTAGE
        protectionBits and 0x0008 != 0 -> BatteryStatus.UNDER_VOLTAGE
        protectionBits and 0x0001 != 0 -> BatteryStatus.CELL_OVER_VOLTAGE
        protectionBits and 0x0002 != 0 -> BatteryStatus.CELL_UNDER_VOLTAGE
        protectionBits and 0x0100 != 0 -> BatteryStatus.OVER_CURRENT_CHARGE
        protectionBits and 0x0200 != 0 -> BatteryStatus.OVER_CURRENT_DISCHARGE
        protectionBits and 0x0010 != 0 -> BatteryStatus.OVER_TEMPERATURE
        protectionBits and 0x0020 != 0 -> BatteryStatus.UNDER_TEMPERATURE
        protectionBits and 0x0400 != 0 -> BatteryStatus.SHORT_CIRCUIT
        else -> BatteryStatus.UNKNOWN
    }
}
