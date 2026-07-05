package com.e_rickshawbatteryunlock.developerasaad.domain.repository

import com.e_rickshawbatteryunlock.developerasaad.domain.model.SavedBattery
import kotlinx.coroutines.flow.Flow

/**
 * Manages persistence of previously connected batteries.
 *
 * Implementations use Room for device metadata and [SecureStorage] for passwords.
 * The two stores are accessed through this single interface so that neither
 * the domain layer nor the presentation layer needs to know the storage details.
 */
interface SavedBatteryRepository {

    /**
     * Observes the full list of saved batteries, ordered by [SavedBattery.lastConnectedAt]
     * descending (most recently used first).
     */
    fun observeSavedBatteries(): Flow<List<SavedBattery>>

    /**
     * Saves or updates a battery entry.
     *
     * If a battery with the same [SavedBattery.address] already exists, the
     * display name and last-connected timestamp are updated. The password
     * stored in [SecureStorage] is not modified by this call.
     */
    suspend fun saveBattery(battery: SavedBattery)

    /**
     * Removes the battery from Room and deletes its password from SecureStorage.
     *
     * @param address The MAC address of the battery to delete.
     */
    suspend fun deleteBattery(address: String)

    /**
     * Retrieves the stored password for a battery, or null if none is saved.
     *
     * @param address The MAC address to look up.
     * @return The plaintext password (only in memory — never written to disk in plain text).
     */
    suspend fun getPassword(address: String): String?

    /**
     * Saves or updates the password for the given battery address in SecureStorage.
     *
     * @param address  The MAC address to key the password against.
     * @param password The password to store. Pass null to remove the entry.
     */
    suspend fun savePassword(address: String, password: String?)
}
