package com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol

import android.bluetooth.BluetoothGatt
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.jbd.JbdBmsProtocol
import javax.inject.Inject

/**
 * Selects the appropriate [BmsProtocol] implementation based on the GATT services
 * discovered on a connected device.
 *
 * The factory iterates over all registered protocol implementations and returns
 * the first one whose [BmsProtocol.serviceUuids] are present on the device.
 *
 * Adding a new BMS vendor:
 *  1. Create a new class implementing [BmsProtocol].
 *  2. Add an instance to [registeredProtocols] in this factory.
 *  No other changes are required.
 *
 * @param jbdProtocol The JBD/Xiaoxiang protocol implementation (primary target).
 */
class BmsProtocolFactory @Inject constructor(
    private val jbdProtocol: JbdBmsProtocol,
) {
    /**
     * Ordered list of all registered protocol implementations.
     * Evaluated in order — the first matching protocol wins.
     * More specific protocols should be listed before general ones.
     */
    private val registeredProtocols: List<BmsProtocol> = listOf(
        jbdProtocol,
    )

    /**
     * Returns the matching [BmsProtocol] for the given connected [gatt] instance.
     *
     * @param gatt The connected [BluetoothGatt] with already-discovered services.
     * @return The first protocol implementation whose service UUID is present,
     *         or null if no registered protocol matches.
     */
    fun detectProtocol(gatt: BluetoothGatt): BmsProtocol? {
        val discoveredServiceUuids = gatt.services.map { it.uuid }.toSet()
        return registeredProtocols.firstOrNull { protocol ->
            protocol.serviceUuids.any { it in discoveredServiceUuids }
        }
    }
}
