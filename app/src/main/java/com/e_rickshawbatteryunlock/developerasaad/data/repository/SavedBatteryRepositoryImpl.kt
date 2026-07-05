package com.e_rickshawbatteryunlock.developerasaad.data.repository

import com.e_rickshawbatteryunlock.developerasaad.data.local.db.SavedBatteryDao
import com.e_rickshawbatteryunlock.developerasaad.data.local.db.SavedBatteryEntity
import com.e_rickshawbatteryunlock.developerasaad.data.local.security.SecureStorage
import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SavedBatteryRepository] implementation.
 *
 * Coordinates between [SavedBatteryDao] (Room database) and [SecureStorage]
 * (EncryptedSharedPreferences) to provide a unified saved battery storage API.
 *
 * All database and IO operations run on [Dispatchers.IO].
 */
@Singleton
class SavedBatteryRepositoryImpl @Inject constructor(
    private val dao: SavedBatteryDao,
    private val secureStorage: SecureStorage,
) : SavedBatteryRepository {

    override fun observeSavedBatteries(): Flow<List<SavedBattery>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveBattery(battery: SavedBattery) = withContext(Dispatchers.IO) {
        dao.upsert(battery.toEntity())
    }

    override suspend fun deleteBattery(address: String) = withContext(Dispatchers.IO) {
        dao.deleteByAddress(address)
        secureStorage.deletePassword(address)
    }

    override suspend fun getPassword(address: String): String? = withContext(Dispatchers.IO) {
        secureStorage.getPassword(address)
    }

    override suspend fun savePassword(address: String, password: String?) =
        withContext(Dispatchers.IO) {
            secureStorage.savePassword(address, password)
        }

    // ─── Mapping ───────────────────────────────────────────────────────────────

    private fun SavedBatteryEntity.toDomain() = SavedBattery(
        address = address,
        displayName = displayName,
        lastConnectedAt = lastConnectedAt,
        hasPassword = hasPassword,
    )

    private fun SavedBattery.toEntity() = SavedBatteryEntity(
        address = address,
        displayName = displayName,
        lastConnectedAt = lastConnectedAt,
        hasPassword = hasPassword,
    )
}
