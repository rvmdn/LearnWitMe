package com.learnwitme.core.theme.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val loader: ThemeLoader,
    private val parser: ThemeParser,
    private val validator: ThemeValidator
) {
    private val cache = mutableMapOf<String, ThemeBundleDto>()
    private val _activeThemeId = MutableStateFlow("default")
    val activeThemeId: StateFlow<String> = _activeThemeId.asStateFlow()

    fun getInstalledThemes(): List<String> {
        return loader.listInstalledThemeIds()
    }

    fun loadTheme(themeId: String): ThemeBundleDto {
        return cache.getOrPut(themeId) {
            val dto = parser.parseBundle(loader, themeId)
            validator.validate(dto)
            dto
        }
    }

    fun setActiveTheme(themeId: String) {
        loadTheme(themeId)
        _activeThemeId.value = themeId
    }

    fun getActiveThemeBundle(): ThemeBundleDto {
        return loadTheme(_activeThemeId.value)
    }
}
