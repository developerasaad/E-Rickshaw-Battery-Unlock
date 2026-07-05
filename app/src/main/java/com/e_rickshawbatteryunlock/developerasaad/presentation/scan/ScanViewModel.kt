package com.e_rickshawbatteryunlock.developerasaad.presentation.scan

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ConnectToBatteryUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.SaveBatteryUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ScanForBatteriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the scan screen.
 *
 * Manages the scan lifecycle, connection initiation, and state exposure.
 * All state mutations go through [_uiState] and are exposed as an immutable [StateFlow].
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanForBatteries: ScanForBatteriesUseCase,
    private val connectToBattery: ConnectToBatteryUseCase,
    private val saveBattery: SaveBatteryUseCase,
    private val bluetoothAdapter: BluetoothAdapter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /** Navigation event: navigate to dashboard after successful connection. */
    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard: StateFlow<Boolean> = _navigateToDashboard.asStateFlow()

    private var scanJob: Job? = null

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasBluetoothPermission = granted) }
        if (granted) checkBluetoothAndStartScan()
    }

    fun onBluetoothStateChanged(enabled: Boolean) {
        _uiState.update { it.copy(isBluetoothEnabled = enabled) }
        if (enabled && _uiState.value.hasBluetoothPermission) startScan()
    }

    fun startScan() {
        if (_uiState.value.isScanning) return
        scanJob?.cancel()

        _uiState.update {
            it.copy(
                isScanning = true,
                discoveredDevices = emptyList(),
                errorMessage = null,
            )
        }

        scanJob = scanForBatteries()
            .onEach { devices ->
                _uiState.update { it.copy(discoveredDevices = devices) }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = error.message ?: "Scan failed unexpectedly",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update { it.copy(isScanning = false) }
    }

    fun connectToDevice(device: BatteryDevice) {
        if (_uiState.value.connectingToAddress != null) return
        stopScan()

        viewModelScope.launch {
            _uiState.update { it.copy(connectingToAddress = device.address, errorMessage = null) }

            val result = connectToBattery(device)

            if (result.isSuccess) {
                // Save battery to history on successful connection
                saveBattery(
                    SavedBattery(
                        address = device.address,
                        displayName = device.name ?: device.address,
                        lastConnectedAt = System.currentTimeMillis(),
                        hasPassword = false,
                    )
                )
                _navigateToDashboard.value = true
            } else {
                _uiState.update {
                    it.copy(
                        connectingToAddress = null,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Connection failed. Please try again.",
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onNavigatedToDashboard() {
        _navigateToDashboard.value = false
        _uiState.update { it.copy(connectingToAddress = null) }
    }

    private fun checkBluetoothAndStartScan() {
        val enabled = bluetoothAdapter.isEnabled
        _uiState.update { it.copy(isBluetoothEnabled = enabled) }
        if (enabled) startScan()
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
