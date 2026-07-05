package com.e_rickshawbatteryunlock.developerasaad.presentation.scan

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice

/**
 * Complete UI state for the scan screen.
 *
 * All state is immutable — the ViewModel emits new instances via [StateFlow].
 */
data class ScanUiState(
    /** Whether a BLE scan is currently in progress. */
    val isScanning: Boolean = false,
    /** List of batteries discovered during the current scan session. */
    val discoveredDevices: List<BatteryDevice> = emptyList(),
    /** MAC address of the device currently being connected to (null if none). */
    val connectingToAddress: String? = null,
    /** Human-readable error message to display, null if no error. */
    val errorMessage: String? = null,
    /** Whether Bluetooth permission has been granted. */
    val hasBluetoothPermission: Boolean = false,
    /** Whether Bluetooth is currently enabled on the device. */
    val isBluetoothEnabled: Boolean = false,
)
