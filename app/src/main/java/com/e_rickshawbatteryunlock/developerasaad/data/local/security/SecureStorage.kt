package com.e_rickshawbatteryunlock.developerasaad.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SecureStorage"
private const val PREFS_FILE_NAME = "battery_secure_prefs"
private const val KEY_ALIAS = "battery_unlock_master_key"

/**
 * Secure key-value store for BMS passwords.
 *
 * Backed by [EncryptedSharedPreferences] with a master key stored in the
 * Android Keystore. All values are AES-256-GCM encrypted on disk.
 *
 * Keys are BMS MAC addresses (e.g., "AA:BB:CC:DD:EE:FF").
 * Values are the plaintext password strings — they are encrypted at rest
 * but decrypted in memory on retrieval.
 *
 * Security guarantees:
 *  - Passwords are never written to disk in plaintext.
 *  - The master key is stored in the Android Keystore hardware security module.
 *  - Data can only be decrypted on the same device with the same key.
 *  - Keys are non-exportable from the Keystore.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val sharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context, KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences: ${e.message}")
            throw IllegalStateException(
                "Secure storage could not be initialized. " +
                    "The device may not support hardware-backed key storage.",
                e
            )
        }
    }

    /**
     * Stores or updates the password for [address].
     *
     * @param address  The BMS MAC address (used as the storage key).
     * @param password The password to store. Pass null to remove the entry.
     */
    fun savePassword(address: String, password: String?) {
        if (password == null) {
            sharedPreferences.edit().remove(address).apply()
        } else {
            sharedPreferences.edit().putString(address, password).apply()
        }
    }

    /**
     * Retrieves the stored password for [address].
     *
     * @return The decrypted password string, or null if no password is stored.
     */
    fun getPassword(address: String): String? = sharedPreferences.getString(address, null)

    /**
     * Returns true if a password is stored for [address].
     */
    fun hasPassword(address: String): Boolean = sharedPreferences.contains(address)

    /**
     * Removes the stored password for [address].
     */
    fun deletePassword(address: String) {
        sharedPreferences.edit().remove(address).apply()
    }
}
