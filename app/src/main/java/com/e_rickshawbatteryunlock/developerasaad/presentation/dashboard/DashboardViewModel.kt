package com.e_rickshawbatteryunlock.developerasaad.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.DisconnectUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.GetRecoveryCapabilityUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ObserveBatteryInfoUseCase
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ObserveConnectionStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeConnectionState: ObserveConnectionStateUseCase,
    private val observeBatteryInfo: ObserveBatteryInfoUseCase,
    private val getRecoveryCapability: GetRecoveryCapabilityUseCase,
    private val disconnect: DisconnectUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        observeConnectionState()
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
                if (state == ConnectionState.DISCONNECTED && !_uiState.value.isDisconnecting) {
                    // Unexpected disconnect
                    _navigateBack.value = true
                }
            }
            .launchIn(viewModelScope)

        observeBatteryInfo()
            .onEach { info ->
                _uiState.update { it.copy(batteryInfo = info) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val capability = getRecoveryCapability()
            _uiState.update { it.copy(capability = capability) }
        }
    }

    fun onDisconnect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDisconnecting = true) }
            disconnect()
            _navigateBack.value = true
        }
    }

    fun onNavigatedBack() {
        _navigateBack.value = false
        _uiState.update { it.copy(isDisconnecting = false) }
    }
}
