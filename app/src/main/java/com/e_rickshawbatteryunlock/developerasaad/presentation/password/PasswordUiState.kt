package com.e_rickshawbatteryunlock.developerasaad.presentation.password

data class PasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showCurrentPassword: Boolean = false,
    val showNewPassword: Boolean = false,
)
