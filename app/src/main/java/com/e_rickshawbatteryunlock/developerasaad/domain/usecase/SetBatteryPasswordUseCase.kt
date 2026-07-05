package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import javax.inject.Inject

/**
 * Sets, updates, or removes the password on the connected BMS and in local secure storage.
 *
 * If the BLE write succeeds, the password is persisted in [SecureStorage].
 * If the BLE write fails, the local storage is not updated so they remain in sync.
 *
 * @param address  MAC address of the battery (used as the secure storage key).
 * @param password The new password, or null to remove the password.
 */
class SetBatteryPasswordUseCase @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val savedBatteryRepository: SavedBatteryRepository,
) {
    suspend operator fun invoke(address: String, password: String?): Result<Unit> {
        val bleResult = batteryRepository.setPassword(password)
        if (bleResult.isSuccess) {
            savedBatteryRepository.savePassword(address, password)
        }
        return bleResult
    }
}
