package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import javax.inject.Inject

/** Cleanly disconnects from the currently connected BMS. */
class DisconnectUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    suspend operator fun invoke() = repository.disconnect()
}
