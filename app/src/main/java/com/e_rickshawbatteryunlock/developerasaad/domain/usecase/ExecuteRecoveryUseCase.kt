package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import javax.inject.Inject

/**
 * Executes the primary battery recovery operation.
 *
 * Before writing the command, the repository checks that the connected BMS
 * exposes the discharge control characteristic. If it does not, this returns
 * [RecoveryResult.Unsupported] without sending any write to the device.
 */
class ExecuteRecoveryUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    suspend operator fun invoke(): RecoveryResult = repository.executeRecovery()
}
