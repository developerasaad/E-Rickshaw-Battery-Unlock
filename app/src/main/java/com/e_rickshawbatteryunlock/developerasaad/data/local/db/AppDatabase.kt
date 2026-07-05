package com.e_rickshawbatteryunlock.developerasaad.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the E-Rickshaw Battery Unlock application.
 *
 * Contains only the [SavedBatteryEntity] table. Passwords are stored separately
 * in [SecureStorage] (Android Keystore-backed EncryptedSharedPreferences) and
 * are NOT part of this database schema.
 *
 * Version history:
 *  - 1: Initial schema — saved_batteries table
 *
 * [exportSchema] is set to false because there is no need to include the schema
 * in the app bundle for this project. Enable it if you add migrations.
 */
@Database(
    entities = [SavedBatteryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedBatteryDao(): SavedBatteryDao
}
