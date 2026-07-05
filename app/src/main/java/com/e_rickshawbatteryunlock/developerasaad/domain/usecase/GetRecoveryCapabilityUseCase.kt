package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import javax.inject.Inject

/** Returns the capabilities of the currently connected BMS. */
class GetRecoveryCapabilityUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    suspend operator fun invoke(): RecoveryCapability = repository.getRecoveryCapability()
}
