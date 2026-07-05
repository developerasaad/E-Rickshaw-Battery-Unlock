package com.e_rickshawbatteryunlock.developerasaad.presentation.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.e_rickshawbatteryunlock.developerasaad.domain.usecase.SetBatteryPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the password management screen.
 *
 * Validates the new password locally before sending the BLE command,
 * preventing unnecessary BLE writes for obviously invalid input.
 */
@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val setPassword: SetBatteryPasswordUseCase,
) : ViewModel() {

    // Placeholder address — in a real implementation this comes from the
    // connected device state shared via a session holder or SavedStateHandle.
    // For now the BleManager handles the connected device internally.
    private val connectedDeviceAddress = "CONNECTED"

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState: StateFlow<PasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPassword = value, errorMessage = null) }
    }

    fun onToggleShowCurrentPassword() {
        _uiState.update { it.copy(showCurrentPassword = !it.showCurrentPassword) }
    }

    fun onToggleShowNewPassword() {
        _uiState.update { it.copy(showNewPassword = !it.showNewPassword) }
    }

    fun onSetPassword() {
        val state = _uiState.value

        // Local validation before sending BLE command
        if (state.newPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "New password cannot be empty.") }
            return
        }
        if (state.newPassword.length < 4) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 4 characters.") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = setPassword(
                address = connectedDeviceAddress,
                password = state.newPassword,
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password set successfully.",
                        newPassword = "",
                        confirmPassword = "",
                        currentPassword = "",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Failed to set password on the BMS.",
                    )
                }
            }
        }
    }

    fun onRemovePassword() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = setPassword(
                address = connectedDeviceAddress,
                password = null,
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password removed successfully.",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Failed to remove password.",
                    )
                }
            }
        }
    }

    fun onDismissMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
