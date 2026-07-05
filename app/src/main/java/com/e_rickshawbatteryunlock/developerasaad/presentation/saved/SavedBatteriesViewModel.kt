package com.e_rickshawbatteryunlock.developerasaad.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ConnectToBatteryUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.DeleteSavedBatteryUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.GetSavedBatteriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedBatteriesViewModel @Inject constructor(
    private val getSavedBatteries: GetSavedBatteriesUseCase,
    private val deleteSavedBattery: DeleteSavedBatteryUseCase,
    private val connectToBattery: ConnectToBatteryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedBatteriesUiState())
    val uiState: StateFlow<SavedBatteriesUiState> = _uiState.asStateFlow()

    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard: StateFlow<Boolean> = _navigateToDashboard.asStateFlow()

    init {
        getSavedBatteries()
            .onEach { batteries ->
                _uiState.update { it.copy(batteries = batteries) }
            }
            .launchIn(viewModelScope)
    }

    fun onDeleteBattery(address: String) {
        viewModelScope.launch {
            deleteSavedBattery(address)
        }
    }

    fun onConnectBattery(address: String, name: String?) {
        if (_uiState.value.isConnectingTo != null) return
        _uiState.update { it.copy(isConnectingTo = address, errorMessage = null) }
        viewModelScope.launch {
            val device = BatteryDevice(name = name, address = address, rssi = 0)
            val result = connectToBattery(device)
            if (result.isSuccess) {
                _navigateToDashboard.value = true
            } else {
                _uiState.update {
                    it.copy(
                        isConnectingTo = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Connection failed",
                    )
                }
            }
        }
    }

    fun onNavigatedToDashboard() {
        _navigateToDashboard.value = false
        _uiState.update { it.copy(isConnectingTo = null) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
