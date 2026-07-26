package com.learnwitme.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.learnwitme.core.security.CryptographyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_preferences")

@Singleton
class EncryptedDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getStringFlow(key: String, defaultValue: String = ""): Flow<String> {
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            val encryptedValue = preferences[prefKey]
            if (encryptedValue != null) {
                try {
                    CryptographyManager.decrypt(encryptedValue)
                } catch (e: Exception) {
                    defaultValue
                }
            } else {
                defaultValue
            }
        }
    }

    suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        val encryptedValue = CryptographyManager.encrypt(value)
        context.dataStore.edit { preferences ->
            preferences[prefKey] = encryptedValue
        }
    }

    suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
