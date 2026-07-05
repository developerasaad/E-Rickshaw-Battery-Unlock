package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import javax.inject.Inject

/** Persists a connected battery to local storage. */
class SaveBatteryUseCase @Inject constructor(
    private val repository: SavedBatteryRepository,
) {
    suspend operator fun invoke(battery: SavedBattery) = repository.saveBattery(battery)
}
