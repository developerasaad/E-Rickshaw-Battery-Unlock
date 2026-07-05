package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the list of previously connected batteries from local storage. */
class GetSavedBatteriesUseCase @Inject constructor(
    private val repository: SavedBatteryRepository,
) {
    operator fun invoke(): Flow<List<SavedBattery>> = repository.observeSavedBatteries()
}
