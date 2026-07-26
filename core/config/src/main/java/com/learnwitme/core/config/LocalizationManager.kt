package com.learnwitme.core.config

import android.content.Context
import com.learnwitme.core.storage.EncryptedDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class DictionarySchema(
    val languageCode: String = "",
    val languageName: String = "",
    val strings: Map<String, String> = emptyMap()
)

@Singleton
class LocalizationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager,
    private val encryptedDataStore: EncryptedDataStore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val stringDictionary = ConcurrentHashMap<String, String>()

    private val _currentLanguage = MutableStateFlow(configManager.config.languageConfig.defaultLanguage)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        scope.launch {
            encryptedDataStore.getStringFlow("user_language_preference", configManager.config.languageConfig.defaultLanguage)
                .collect { langCode ->
                    val targetLang = langCode.ifBlank { configManager.config.languageConfig.defaultLanguage }
                    _currentLanguage.value = targetLang
                    loadDictionary(targetLang)
                }
        }
    }

    fun loadDictionary(langCode: String) {
        try {
            val fileName = "metadata/i18n/$langCode.json"
            val jsonString = context.assets.open(fileName).use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            val dictionary = json.decodeFromString<DictionarySchema>(jsonString)
            stringDictionary.clear()
            stringDictionary.putAll(dictionary.strings)
            Timber.d("Localization loaded successfully for code: %s (%d strings)", langCode, dictionary.strings.size)
        } catch (e: Exception) {
            Timber.w(e, "Failed to load dictionary for code: %s. Falling back to default/empty.", langCode)
            if (langCode != "en") {
                loadDictionary("en")
            }
        }
    }

    fun getString(key: String, fallback: String = key): String {
        return stringDictionary[key] ?: fallback
    }

    suspend fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
        encryptedDataStore.putString("user_language_preference", langCode)
        loadDictionary(langCode)
    }
}
