package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the BLE connection lifecycle state. */
class ObserveConnectionStateUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    operator fun invoke(): Flow<ConnectionState> = repository.observeConnectionState()
}
