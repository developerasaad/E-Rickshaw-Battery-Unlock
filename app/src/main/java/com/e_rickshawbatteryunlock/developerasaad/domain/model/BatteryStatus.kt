package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * Enum representing the overall health status of a battery cell group
 * as reported by the BMS protection register.
 *
 * Maps directly to the protection status bits in the JBD/Xiaoxiang protocol
 * response at bytes 20–21 of the basic information frame.
 */
enum class BatteryStatus {
    /** All parameters within normal operating range. */
    NORMAL,

    /** Pack voltage exceeds the configured over-voltage threshold. */
    OVER_VOLTAGE,

    /** Pack voltage has dropped below the configured under-voltage threshold. */
    UNDER_VOLTAGE,

    /** A single cell voltage exceeds the configured over-voltage threshold. */
    CELL_OVER_VOLTAGE,

    /** A single cell voltage has dropped below the under-voltage threshold. */
    CELL_UNDER_VOLTAGE,

    /** Discharge current exceeds the configured over-current threshold. */
    OVER_CURRENT_DISCHARGE,

    /** Charge current exceeds the configured over-current threshold. */
    OVER_CURRENT_CHARGE,

    /** Temperature exceeds the configured high-temperature threshold. */
    OVER_TEMPERATURE,

    /** Temperature is below the configured low-temperature threshold (charge protection). */
    UNDER_TEMPERATURE,

    /** Short circuit protection has triggered. */
    SHORT_CIRCUIT,

    /**
     * Status could not be determined — BMS did not return a readable
     * protection status register value.
     */
    UNKNOWN,
}
