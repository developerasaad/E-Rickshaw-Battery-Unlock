package com.e_rickshawbatteryunlock.developerasaad.presentation.dashboard

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability

data class DashboardUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val batteryInfo: BatteryInfo = BatteryInfo.EMPTY,
    val capability: RecoveryCapability = RecoveryCapability.NONE,
    val isDisconnecting: Boolean = false,
)
