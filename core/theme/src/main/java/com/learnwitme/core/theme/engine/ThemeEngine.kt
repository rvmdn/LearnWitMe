package com.learnwitme.core.theme.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeEngine @Inject constructor(
    private val repository: ThemeRepository
) {
    private val _currentBundle = MutableStateFlow(repository.getActiveThemeBundle())
    val currentBundle: StateFlow<ThemeBundleDto> = _currentBundle.asStateFlow()

    fun switchTheme(themeId: String) {
        repository.setActiveTheme(themeId)
        _currentBundle.value = repository.getActiveThemeBundle()
    }

    fun parseColorHex(hex: String): Long {
        val clean = hex.removePrefix("#")
        return if (clean.length == 6) {
            0xFF000000 or clean.toLong(16)
        } else if (clean.length == 8) {
            clean.toLong(16)
        } else {
            0xFF0F172A
        }
    }
}
