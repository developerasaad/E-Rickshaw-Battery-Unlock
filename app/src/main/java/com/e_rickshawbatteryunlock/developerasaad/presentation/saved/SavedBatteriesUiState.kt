package com.e_rickshawbatteryunlock.developerasaad.presentation.saved

import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery

data class SavedBatteriesUiState(
    val batteries: List<SavedBattery> = emptyList(),
    val isConnectingTo: String? = null,
    val errorMessage: String? = null,
)
