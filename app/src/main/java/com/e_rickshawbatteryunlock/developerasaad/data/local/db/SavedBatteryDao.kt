package com.e_rickshawbatteryunlock.developerasaad.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for [SavedBatteryEntity].
 *
 * Uses [OnConflictStrategy.REPLACE] on insert so that reconnecting to a previously
 * saved battery updates its [SavedBatteryEntity.lastConnectedAt] and name without
 * requiring a separate UPDATE query.
 */
@Dao
interface SavedBatteryDao {

    /**
     * Observes all saved batteries, ordered by most recently connected first.
     *
     * Returns a [Flow] that re-emits whenever the table changes, keeping the UI
     * in sync without polling.
     */
    @Query("SELECT * FROM saved_batteries ORDER BY lastConnectedAt DESC")
    fun observeAll(): Flow<List<SavedBatteryEntity>>

    /**
     * Inserts or replaces a battery record.
     *
     * If a record with the same [SavedBatteryEntity.address] exists, it is replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedBatteryEntity)

    /**
     * Deletes the battery record with the given [address].
     *
     * @param address The MAC address of the battery to remove.
     */
    @Query("DELETE FROM saved_batteries WHERE address = :address")
    suspend fun deleteByAddress(address: String)
}
