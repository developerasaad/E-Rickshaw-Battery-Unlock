package com.e_rickshawbatteryunlock.developerasaad.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a previously connected BMS device.
 *
 * The MAC [address] is the primary key — it is the globally unique identifier
 * for a Bluetooth device. Passwords are NOT stored here; they live in
 * [SecureStorage] keyed by this same address.
 *
 * @param address           MAC address in AA:BB:CC:DD:EE:FF format.
 * @param displayName       Device advertised name or user-provided alias.
 * @param lastConnectedAt   Epoch milliseconds of last successful connection.
 * @param hasPassword       Whether a password entry exists in SecureStorage.
 */
@Entity(tableName = "saved_batteries")
data class SavedBatteryEntity(
    @PrimaryKey
    val address: String,
    val displayName: String,
    val lastConnectedAt: Long,
    val hasPassword: Boolean,
)
