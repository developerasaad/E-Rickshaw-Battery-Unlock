package com.e_rickshawbatteryunlock.developerasaad.data.repository

import com.e_rickshawbatteryunlock.developerasaad.data.ble.BleManager
import com.e_rickshawbatteryunlock.developerasaad.data.ble.BleScanner
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryDevice
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryResult
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [BatteryRepository] implementation.
 *
 * Bridges domain-layer repository calls to the BLE infrastructure components:
 *  - [BleScanner] for device discovery
 *  - [BleManager] for connection lifecycle and protocol operations
 *
 * This class contains no business logic — it only translates between the
 * domain interface and the data-layer implementations.
 */
@Singleton
class BatteryRepositoryImpl @Inject constructor(
    private val bleScanner: BleScanner,
    private val bleManager: BleManager,
) : BatteryRepository {

    override fun scanForBatteries(): Flow<List<BatteryDevice>> = bleScanner.scan()

    override suspend fun connect(device: BatteryDevice): Result<Unit> =
        bleManager.connect(device.address)

    override fun observeConnectionState(): Flow<ConnectionState> =
        bleManager.observeConnectionState()

    override fun observeBatteryInfo(): Flow<BatteryInfo> =
        bleManager.observeBatteryInfo()

    override suspend fun getRecoveryCapability(): RecoveryCapability =
        bleManager.getCapability()

    override suspend fun executeRecovery(): RecoveryResult =
        bleManager.executeRecovery()

    override suspend fun setPassword(password: String?): Result<Unit> =
        bleManager.setPassword(password)

    override suspend fun disconnect() {
        bleManager.disconnect()
    }
}
