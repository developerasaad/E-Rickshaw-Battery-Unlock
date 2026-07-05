package com.e_rickshawbatteryunlock.developerasaad.di

import android.content.Context
import androidx.room.Room
import com.e_rickshawbatteryunlock.developerasaad.data.local.db.AppDatabase
import com.e_rickshawbatteryunlock.developerasaad.data.local.db.SavedBatteryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing Room database bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "battery_unlock_db"
    ).build()

    @Provides
    @Singleton
    fun provideSavedBatteryDao(database: AppDatabase): SavedBatteryDao =
        database.savedBatteryDao()
}
