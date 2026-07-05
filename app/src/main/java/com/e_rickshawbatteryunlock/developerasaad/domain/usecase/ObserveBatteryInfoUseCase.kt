package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes live battery telemetry from the connected BMS. */
class ObserveBatteryInfoUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    operator fun invoke(): Flow<BatteryInfo> = repository.observeBatteryInfo()
}
