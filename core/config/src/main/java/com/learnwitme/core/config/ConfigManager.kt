package com.learnwitme.core.config

import android.content.Context
import com.learnwitme.core.storage.EncryptedDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptedDataStore: EncryptedDataStore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
    }

    private val localOverrides = ConcurrentHashMap<String, Boolean>()
    private val scope = CoroutineScope(Dispatchers.IO)

    var config: AppConfig = AppConfig()
        private set

    init {
        loadConfig()
        loadOverrides()
    }

    fun loadConfig() {
        try {
            val jsonString = context.assets.open("metadata/config.json").use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            config = json.decodeFromString<AppConfig>(jsonString)
            Timber.d("Metadata loaded successfully: %s", config)
        } catch (e: Exception) {
            Timber.e(e, "Failed to load config.json. Initializing with fallback.")
            config = AppConfig()
        }
    }

    private fun loadOverrides() {
        config.featureFlags.keys.forEach { flag ->
            scope.launch {
                encryptedDataStore.getStringFlow("feature_$flag").collect { value ->
                    if (value.isNotEmpty()) {
                        localOverrides[flag] = value.toBoolean()
                        Timber.d("Merged override for %s: %s", flag, value)
                    }
                }
            }
        }
    }

    fun isFeatureEnabled(featureKey: String): Boolean {
        return localOverrides[featureKey] ?: config.featureFlags[featureKey] ?: false
    }

    suspend fun setFeatureFlagOverride(featureKey: String, enabled: Boolean) {
        localOverrides[featureKey] = enabled
        encryptedDataStore.putString("feature_$featureKey", enabled.toString())
        Timber.d("Saved override for %s: %s", featureKey, enabled)
    }

    fun getActiveMenus(): List<MenuConfig> {
        val baseMenus = config.menus.toMutableList()
        if (isFeatureEnabled("ai_features")) {
            if (baseMenus.none { it.route == "ai" }) {
                baseMenus.add(
                    MenuConfig(
                        id = "menu_ai",
                        title = "AI Chat",
                        iconName = "developer",
                        route = "ai",
                        group = "main",
                        order = 7
                    )
                )
            }
        }
        if (isFeatureEnabled("ocr_features")) {
            if (baseMenus.none { it.route == "ocr" }) {
                baseMenus.add(
                    MenuConfig(
                        id = "menu_ocr",
                        title = "OCR Scan",
                        iconName = "developer",
                        route = "ocr",
                        group = "main",
                        order = 8
                    )
                )
            }
        }
        return baseMenus.sortedBy { it.order }
    }

    fun getActiveScreens(): List<ScreenConfig> {
        val baseScreens = config.navigation.screens.toMutableList()
        if (isFeatureEnabled("ai_features")) {
            if (baseScreens.none { it.route == "ai" }) {
                baseScreens.add(
                    ScreenConfig(
                        route = "ai",
                        className = "AiScreen",
                        title = "AI Assistant"
                    )
                )
            }
        }
        if (isFeatureEnabled("ocr_features")) {
            if (baseScreens.none { it.route == "ocr" }) {
                baseScreens.add(
                    ScreenConfig(
                        route = "ocr",
                        className = "OcrScreen",
                        title = "OCR Scanner"
                    )
                )
            }
        }
        return baseScreens
    }

    fun getApiEndpoint(endpointKey: String): String? {
        return config.apiEndpoints[endpointKey]
    }
}
