package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * A previously connected battery stored in the local database.
 *
 * Passwords are NOT stored directly in this model. The [SavedBattery.address]
 * is used as a key to look up the password from [SecureStorage] separately,
 * keeping sensitive data out of the Room database.
 *
 * @param address           MAC address in AA:BB:CC:DD:EE:FF format (primary key).
 * @param displayName       User-visible name (device advertised name or user-set alias).
 * @param lastConnectedAt   Epoch milliseconds of the last successful connection.
 * @param hasPassword       Whether a password is saved for this device in SecureStorage.
 */
data class SavedBattery(
    val address: String,
    val displayName: String,
    val lastConnectedAt: Long,
    val hasPassword: Boolean,
)
