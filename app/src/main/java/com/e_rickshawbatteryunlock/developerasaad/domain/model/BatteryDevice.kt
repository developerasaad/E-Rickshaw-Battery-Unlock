package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * Represents a BLE battery device discovered during a scan.
 *
 * @param name         Advertised device name (may be null if not advertised).
 * @param address      MAC address in AA:BB:CC:DD:EE:FF format.
 * @param rssi         Received signal strength indicator in dBm.
 */
data class BatteryDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
)
