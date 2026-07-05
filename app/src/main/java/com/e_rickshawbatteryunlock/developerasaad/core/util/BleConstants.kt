package com.e_rickshawbatteryunlock.developerasaad.core.util

import java.util.UUID

/**
 * BLE constants for the JBD / Xiaoxiang / Generic Chinese BMS protocol.
 *
 * These UUIDs and command bytes are documented in the JBD protocol specification
 * and confirmed by multiple open-source implementations:
 *  - https://github.com/simat/BatteryMonitor/wiki/Generic-Chinese-Bluetooth-BMS-communication-protocol
 *  - https://github.com/KrystianD/smart_bms
 *  - https://github.com/ForrestFire0/GenericBMSArduino
 *
 * Protocol framing (JBD/Xiaoxiang):
 *   Request:  0xDD 0xA5 <cmd> 0x00 <checksum_hi> <checksum_lo> 0x77
 *   Response: 0xDD 0x<cmd> 0x00 <len> <data...> <checksum_hi> <checksum_lo> 0x77
 *   Error:    0xDD 0x<cmd> 0x80 <len> <error_code> <checksum_hi> <checksum_lo> 0x77
 */
object BleConstants {

    // ─── JBD / Xiaoxiang BMS Service (Primary Target) ─────────────────────────

    /**
     * Primary GATT service UUID for JBD/Xiaoxiang BMS modules.
     *
     * This is the FFF0 service in the standard Bluetooth SIG short UUID space,
     * expanded to the full 128-bit format required by Android's BluetoothGatt API.
     */
    val JBD_SERVICE_UUID: UUID = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb")

    /**
     * Notification/read characteristic — carries BMS responses (telemetry frames).
     * This characteristic must have the NOTIFY property; enable notifications
     * by writing the CCCD descriptor to 0x01 0x00.
     */
    val JBD_NOTIFY_CHAR_UUID: UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")

    /**
     * Write-without-response characteristic — carries command frames sent to the BMS.
     * Use WRITE_TYPE_NO_RESPONSE to avoid waiting for a write acknowledgement,
     * as the BMS response arrives asynchronously on the notify characteristic.
     */
    val JBD_WRITE_CHAR_UUID: UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb")

    // ─── JBD Protocol Frame Constants ─────────────────────────────────────────

    /** Start byte for every JBD request and response frame. */
    const val FRAME_START: Byte = 0xDD.toByte()

    /** Request identifier byte (always follows FRAME_START in a request). */
    const val FRAME_REQUEST: Byte = 0xA5.toByte()

    /** Response success flag in byte[2] of a response frame. */
    const val FRAME_RESPONSE_OK: Byte = 0x00

    /** Response error flag in byte[2] of a response frame. */
    const val FRAME_RESPONSE_ERROR: Byte = 0x80.toByte()

    /** End-of-record byte that terminates every frame. */
    const val FRAME_END: Byte = 0x77

    // ─── JBD Command Codes ────────────────────────────────────────────────────

    /** Read basic BMS information (voltage, current, SoC, temp, MOS status). */
    const val CMD_READ_BASIC_INFO: Byte = 0x03

    /** Read individual cell voltages. */
    const val CMD_READ_CELL_VOLTAGES: Byte = 0x04

    /** Read BMS device name string. */
    const val CMD_READ_DEVICE_NAME: Byte = 0x05

    /**
     * Write control command — used for MOS switch control and password operations.
     *
     * For MOS control, the data payload is a single byte:
     *  - 0x01 0x01 = Enable discharge (charge MOS ON, discharge MOS ON)
     *  - 0x01 0x00 = Disable discharge (charge MOS ON, discharge MOS OFF)
     *  - 0x00 0x01 = Disable charge (charge MOS OFF, discharge MOS ON)
     *  - 0x00 0x00 = Disable both (full shutdown)
     *
     * The actual sub-command byte identifies the operation; 0xE1 is the standard
     * MOS control register for JBD hardware.
     */
    const val CMD_WRITE_MOS_CONTROL: Byte = 0xE1.toByte()

    // ─── MOS Control Data Bytes ───────────────────────────────────────────────

    /** Data byte: enable both charge and discharge MOSFETs (normal operation). */
    const val MOS_BOTH_ON: Byte = 0x03

    /** Data byte: enable discharge MOS only (charge disabled). */
    const val MOS_DISCHARGE_ONLY: Byte = 0x02

    /** Data byte: enable charge MOS only (discharge disabled). */
    const val MOS_CHARGE_ONLY: Byte = 0x01

    /** Data byte: disable both MOSFETs (protective shutdown). */
    const val MOS_BOTH_OFF: Byte = 0x00

    // ─── Basic Info Response Field Offsets ────────────────────────────────────
    // All offsets are relative to the start of the DATA section of the response
    // (i.e., after the header: DD, cmd, 00, len).

    /** Bytes 0–1: Total pack voltage in 10mV units. Multiply by 10 for mV. */
    const val BASIC_OFFSET_PACK_VOLTAGE = 0

    /** Bytes 2–3: Net current in 10mA units (signed). Positive = charging. */
    const val BASIC_OFFSET_CURRENT = 2

    /** Bytes 4–5: Remaining capacity in 10mAh units. */
    const val BASIC_OFFSET_REMAINING_CAP = 4

    /** Bytes 6–7: Nominal full capacity in 10mAh units. */
    const val BASIC_OFFSET_FULL_CAP = 6

    /** Bytes 8–9: Cycle count. */
    const val BASIC_OFFSET_CYCLE_COUNT = 8

    /** Bytes 10–11: Production date (BCD: YYYYMMDD). */
    const val BASIC_OFFSET_PRODUCTION_DATE = 10

    /** Bytes 12–13: Balancing cells bitmask (cells 1–16). */
    const val BASIC_OFFSET_BALANCE_LOW = 12

    /** Bytes 14–15: Balancing cells bitmask (cells 17–32). */
    const val BASIC_OFFSET_BALANCE_HIGH = 14

    /** Bytes 16–17: Protection status register (bitmask). */
    const val BASIC_OFFSET_PROTECTION_STATUS = 16

    /** Byte 18: BMS software version. */
    const val BASIC_OFFSET_SW_VERSION = 18

    /** Byte 19: State of charge (0–100%). */
    const val BASIC_OFFSET_SOC = 19

    /**
     * Byte 20: MOS status byte.
     *  Bit 0 = Charge MOS (1 = ON)
     *  Bit 1 = Discharge MOS (1 = ON)
     */
    const val BASIC_OFFSET_MOS_STATUS = 20

    /** Byte 21: Number of cells in the pack. */
    const val BASIC_OFFSET_CELL_COUNT = 21

    /** Byte 22: Number of temperature probes. */
    const val BASIC_OFFSET_TEMP_COUNT = 22

    /** Byte 23+: Temperature data starts here; each reading is 2 bytes (0.1°C units). */
    const val BASIC_OFFSET_TEMP_DATA = 23

    // ─── BLE Client Characteristic Configuration Descriptor UUID ──────────────

    /** Standard CCCD UUID for enabling BLE notifications. */
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // ─── Scan / Connection Configuration ──────────────────────────────────────

    /** Maximum number of automatic reconnect attempts before giving up. */
    const val MAX_RECONNECT_ATTEMPTS = 3

    /** Base delay in milliseconds between reconnect attempts (doubles each attempt). */
    const val RECONNECT_BASE_DELAY_MS = 1_000L

    /** Timeout in milliseconds for a single GATT operation (read/write). */
    const val GATT_OPERATION_TIMEOUT_MS = 10_000L

    /** Requested MTU size. JBD frames are small; 256 ensures no fragmentation. */
    const val REQUESTED_MTU = 256
}
