package com.learnwitme.core.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _themes = MutableStateFlow<Map<String, ThemeConfig>>(emptyMap())
    val themes: StateFlow<Map<String, ThemeConfig>> = _themes.asStateFlow()

    private val _currentTheme = MutableStateFlow<ThemeConfig?>(null)
    val currentTheme: StateFlow<ThemeConfig?> = _currentTheme.asStateFlow()

    init {
        loadAvailableThemes()
    }

    fun loadAvailableThemes() {
        try {
            val themeFiles = context.assets.list("themes") ?: emptyArray()
            val themeMap = mutableMapOf<String, ThemeConfig>()
            
            for (fileName in themeFiles) {
                if (fileName.endsWith(".json")) {
                    try {
                        val jsonString = context.assets.open("themes/$fileName").use { inputStream ->
                            inputStream.bufferedReader().readText()
                        }
                        val themeConfig = json.decodeFromString<ThemeConfig>(jsonString)
                        themeMap[themeConfig.name] = themeConfig
                        Timber.d("Loaded dynamic theme: %s", themeConfig.name)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse theme file: %s", fileName)
                    }
                }
            }
            
            _themes.value = themeMap
            
            // Set Forest theme as default if available, otherwise fallback to first
            val defaultTheme = themeMap["Forest"] ?: themeMap.values.firstOrNull()
            _currentTheme.value = defaultTheme
        } catch (e: Exception) {
            Timber.e(e, "Failed to list theme directory")
        }
    }

    fun selectTheme(themeName: String) {
        val selected = _themes.value[themeName]
        if (selected != null) {
            _currentTheme.value = selected
            Timber.d("Selected theme changed to: %s", themeName)
        } else {
            Timber.w("Theme not found: %s", themeName)
        }
    }

    fun getColorScheme(theme: ThemeConfig?): ColorScheme {
        if (theme == null) return lightColorScheme()

        val isDark = theme.isDark
        val primaryHex = theme.colors["primary"] ?: "#6200EE"
        val onPrimaryHex = theme.colors["onPrimary"] ?: "#FFFFFF"
        val primaryContainerHex = theme.colors["primaryContainer"] ?: primaryHex
        val onPrimaryContainerHex = theme.colors["onPrimaryContainer"] ?: onPrimaryHex
        val secondaryHex = theme.colors["secondary"] ?: "#03DAC6"
        val onSecondaryHex = theme.colors["onSecondary"] ?: "#000000"
        val backgroundHex = theme.colors["background"] ?: if (isDark) "#121212" else "#FFFFFF"
        val onBackgroundHex = theme.colors["onBackground"] ?: if (isDark) "#FFFFFF" else "#000000"
        val surfaceHex = theme.colors["surface"] ?: if (isDark) "#121212" else "#FFFFFF"
        val onSurfaceHex = theme.colors["onSurface"] ?: if (isDark) "#FFFFFF" else "#000000"

        val primary = parseComposeColor(primaryHex)
        val onPrimary = parseComposeColor(onPrimaryHex)
        val primaryContainer = parseComposeColor(primaryContainerHex)
        val onPrimaryContainer = parseComposeColor(onPrimaryContainerHex)
        val secondary = parseComposeColor(secondaryHex)
        val onSecondary = parseComposeColor(onSecondaryHex)
        val background = parseComposeColor(backgroundHex)
        val onBackground = parseComposeColor(onBackgroundHex)
        val surface = parseComposeColor(surfaceHex)
        val onSurface = parseComposeColor(onSurfaceHex)

        return if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = secondary,
                onSecondary = onSecondary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = secondary,
                onSecondary = onSecondary,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface
            )
        }
    }

    private fun parseComposeColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.Gray
        }
    }
}
