package com.learnwitme.core.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of DatabasePassphraseProvider using Android KeyStore-backed AES encryption.
 * Generates a 256-bit secure random passphrase, encrypts it via CryptographyManager, and persists it securely.
 */
@Singleton
class KeystoreDatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DatabasePassphraseProvider {

    companion object {
        private const val PREFS_NAME = "learnwitme_sec_store"
        private const val KEY_DB_PASSPHRASE = "secure_db_passphrase_v1"
        private const val PASSPHRASE_LENGTH_BYTES = 32
    }

    @Synchronized
    override fun getPassphrase(): ByteArray {
        val storedEncryptedHex = getStoredPassphraseHex()
        if (storedEncryptedHex.isNotEmpty()) {
            val decryptedHex = CryptographyManager.decrypt(storedEncryptedHex)
            if (decryptedHex.isNotEmpty()) {
                return decryptedHex.toByteArray(Charsets.UTF_8)
            }
        }

        // Generate a cryptographically strong 256-bit random passphrase
        val randomBytes = ByteArray(PASSPHRASE_LENGTH_BYTES)
        SecureRandom().nextBytes(randomBytes)
        val newPassphraseHex = randomBytes.joinToString("") { "%02x".format(it) }

        // Encrypt with Android KeyStore AES-GCM before saving to disk
        val encryptedHex = CryptographyManager.encrypt(newPassphraseHex)
        saveStoredPassphraseHex(encryptedHex)

        return newPassphraseHex.toByteArray(Charsets.UTF_8)
    }

    private fun getStoredPassphraseHex(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DB_PASSPHRASE, "") ?: ""
    }

    private fun saveStoredPassphraseHex(encryptedHex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DB_PASSPHRASE, encryptedHex).apply()
    }
}
