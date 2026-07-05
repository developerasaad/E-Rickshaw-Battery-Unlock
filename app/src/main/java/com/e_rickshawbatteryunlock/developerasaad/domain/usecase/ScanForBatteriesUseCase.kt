package com.e_rickshawbatteryunlock.developerasaad.domain.usecase

import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Starts a BLE scan and returns a [Flow] of discovered compatible batteries.
 *
 * The scan runs for as long as the collecting coroutine is active. Cancel the
 * coroutine to stop scanning and release BLE resources.
 */
class ScanForBatteriesUseCase @Inject constructor(
    private val repository: BatteryRepository,
) {
    operator fun invoke(): Flow<List<BatteryDevice>> = repository.scanForBatteries()
}
