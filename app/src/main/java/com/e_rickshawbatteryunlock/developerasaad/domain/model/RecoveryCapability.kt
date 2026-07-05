package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * Describes the operations the connected BMS hardware supports.
 *
 * Populated after service discovery by interrogating which GATT characteristics
 * are present on the device. The BleManager queries the protocol implementation
 * to determine this — no assumptions are hardcoded.
 *
 * @param canReadBatteryInfo    Whether basic telemetry (voltage, current, temp) is readable.
 * @param canReadCellVoltages   Whether individual cell voltage data is available.
 * @param canEnableDischarge    Whether the discharge MOSFET can be controlled.
 * @param canEnableCharge       Whether the charge MOSFET can be controlled.
 * @param canSetPassword        Whether the BMS supports password configuration.
 * @param vendorName            Human-readable vendor name for display in diagnostics.
 */
data class RecoveryCapability(
    val canReadBatteryInfo: Boolean,
    val canReadCellVoltages: Boolean,
    val canEnableDischarge: Boolean,
    val canEnableCharge: Boolean,
    val canSetPassword: Boolean,
    val vendorName: String,
) {
    companion object {
        /** Represents a device where no supported capabilities were detected. */
        val NONE = RecoveryCapability(
            canReadBatteryInfo = false,
            canReadCellVoltages = false,
            canEnableDischarge = false,
            canEnableCharge = false,
            canSetPassword = false,
            vendorName = "Unknown",
        )
    }
}
