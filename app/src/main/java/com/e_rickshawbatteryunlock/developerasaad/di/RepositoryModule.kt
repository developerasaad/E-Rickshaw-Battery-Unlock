package com.e_rickshawbatteryunlock.developerasaad.di

import com.e_rickshawbatteryunlock.developerasaad.data.repository.BatteryRepositoryImpl
import com.e_rickshawbatteryunlock.developerasaad.data.repository.SavedBatteryRepositoryImpl
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.BatteryRepository
import com.e_rickshawbatteryunlock.developerasaad.domain.repository.SavedBatteryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding domain repository interfaces to their data-layer implementations.
 *
 * Uses [Binds] instead of [dagger.Provides] to generate more efficient code —
 * [Binds] tells Hilt to use the implementation directly without wrapping it in
 * a factory method call.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        impl: BatteryRepositoryImpl,
    ): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindSavedBatteryRepository(
        impl: SavedBatteryRepositoryImpl,
    ): SavedBatteryRepository
}
