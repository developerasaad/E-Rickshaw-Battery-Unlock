package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * Live telemetry snapshot from a connected BMS.
 *
 * All fields are nullable. A null value indicates the connected BMS did not
 * report that measurement — the UI must display "N/A" rather than fabricating
 * a value.
 *
 * Field units follow the JBD/Xiaoxiang protocol specification:
 *  - Voltage: millivolts (mV)
 *  - Current: milliamps (mA), positive = charging, negative = discharging
 *  - Temperature: tenths of a degree Celsius (e.g. 253 = 25.3 °C)
 *  - State of Charge: percentage 0–100
 *  - Remaining capacity: milliamp-hours (mAh)
 *  - Full capacity: milliamp-hours (mAh)
 *  - Cycle count: number of charge/discharge cycles
 *
 * @param packVoltageMillivolts Total pack voltage in mV.
 * @param currentMilliamps      Net current in mA (positive=charge, negative=discharge).
 * @param stateOfChargePercent  Battery percentage reported by BMS.
 * @param remainingCapacityMAh  Remaining capacity in mAh.
 * @param fullCapacityMAh       Nominal full capacity in mAh.
 * @param cycleCount            Number of charge cycles completed.
 * @param temperatures          List of temperature readings in tenths of a degree Celsius.
 *                              Multiple probes possible; first is usually FET temperature.
 * @param cellVoltagesMillivolts Individual cell voltages in mV. Empty if unsupported.
 * @param status                Overall battery protection status.
 * @param isChargingEnabled     Whether the charge MOSFET is open (true = charge allowed).
 * @param isDischargeEnabled    Whether the discharge MOSFET is open (true = discharge allowed).
 * @param balancingCells        Bit mask of cells currently being balanced (0 = none).
 */
data class BatteryInfo(
    val packVoltageMillivolts: Int?,
    val currentMilliamps: Int?,
    val stateOfChargePercent: Int?,
    val remainingCapacityMAh: Int?,
    val fullCapacityMAh: Int?,
    val cycleCount: Int?,
    val temperatures: List<Int>,
    val cellVoltagesMillivolts: List<Int>,
    val status: BatteryStatus,
    val isChargingEnabled: Boolean?,
    val isDischargeEnabled: Boolean?,
    val balancingCells: Int?,
) {
    companion object {
        /** An empty snapshot used as the initial state before any data arrives. */
        val EMPTY = BatteryInfo(
            packVoltageMillivolts = null,
            currentMilliamps = null,
            stateOfChargePercent = null,
            remainingCapacityMAh = null,
            fullCapacityMAh = null,
            cycleCount = null,
            temperatures = emptyList(),
            cellVoltagesMillivolts = emptyList(),
            status = BatteryStatus.UNKNOWN,
            isChargingEnabled = null,
            isDischargeEnabled = null,
            balancingCells = null,
        )
    }
}
