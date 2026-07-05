package com.e_rickshawbatteryunlock.developerasaad.presentation.recovery

import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult

data class RecoveryUiState(
    val isLoading: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val result: RecoveryResult? = null,
    val errorMessage: String? = null,
)
