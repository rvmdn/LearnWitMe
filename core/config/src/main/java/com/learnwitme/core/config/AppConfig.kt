package com.learnwitme.core.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val environment: EnvironmentConfig = EnvironmentConfig(),
    val languageConfig: LanguageConfig = LanguageConfig(),
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val navigation: NavigationConfig = NavigationConfig(),
    val menus: List<MenuConfig> = emptyList(),
    val plugins: Map<String, PluginConfig> = emptyMap(),
    val aiProviders: List<AiProviderConfig> = emptyList(),
    val billingProducts: List<BillingProductConfig> = emptyList(),
    val apiEndpoints: Map<String, String> = emptyMap()
)

@Serializable
data class EnvironmentConfig(
    val name: String = "development",
    val debugEnabled: Boolean = true,
    val sslPinningEnabled: Boolean = false
)

@Serializable
data class LanguageConfig(
    val defaultLanguage: String = "id",
    val supportedLanguages: List<LanguageInfo> = listOf(
        LanguageInfo("id", "Bahasa Indonesia", isDefault = true),
        LanguageInfo("en", "English", isDefault = false)
    )
)

@Serializable
data class LanguageInfo(
    val code: String,
    val name: String,
    val isDefault: Boolean = false
)

@Serializable
data class NavigationConfig(
    val defaultRoute: String = "home",
    val splashRoute: String = "splash",
    val loginRoute: String = "login",
    val screens: List<ScreenConfig> = emptyList()
)

@Serializable
data class ScreenConfig(
    val route: String,
    val className: String, // Dynamic loading / plugin association
    val title: String,
    val requiresAuth: Boolean = false,
    val arguments: List<String> = emptyList()
)

@Serializable
data class MenuConfig(
    val id: String,
    val title: String,
    val iconName: String,
    val route: String,
    val group: String = "main", // "main", "drawer", "bottom"
    val order: Int = 0
)

@Serializable
data class PluginConfig(
    val enabled: Boolean = false,
    val version: String = "1.0.0",
    val properties: Map<String, String> = emptyMap()
)

@Serializable
data class AiProviderConfig(
    val name: String,
    val enabled: Boolean = false,
    val baseUrl: String = "",
    val modelName: String = ""
)

@Serializable
data class BillingProductConfig(
    val id: String,
    val type: String, // "subscription", "consumable"
    val price: String,
    val title: String
)
