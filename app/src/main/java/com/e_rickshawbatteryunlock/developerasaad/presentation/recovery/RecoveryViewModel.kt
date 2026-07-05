package com.e_rickshawbatteryunlock.developerasaad.presentation.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.ExecuteRecoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val executeRecovery: ExecuteRecoveryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    fun onUnlockClicked() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun onConfirmRecovery() {
        _uiState.update { it.copy(showConfirmDialog = false, isLoading = true, result = null) }
        viewModelScope.launch {
            val result = executeRecovery()
            _uiState.update { it.copy(isLoading = false, result = result) }
        }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onRetry() {
        _uiState.update { it.copy(result = null, errorMessage = null) }
    }
}
