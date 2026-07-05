package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import javax.inject.Inject

/** Initiates a GATT connection to the selected [BatteryDevice]. */
class ConnectToBatteryUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    suspend operator fun invoke(device: BatteryDevice): Result<Unit> =
        repository.connect(device)
}
