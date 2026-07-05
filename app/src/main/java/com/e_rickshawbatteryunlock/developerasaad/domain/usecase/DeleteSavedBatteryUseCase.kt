package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import javax.inject.Inject

/** Removes a saved battery and its stored password from local storage. */
class DeleteSavedBatteryUseCase @Inject constructor(
    private val repository: SavedBatteryRepository,
) {
    suspend operator fun invoke(address: String) = repository.deleteBattery(address)
}
