package com.learnwitme.core.theme

import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val name: String,
    val isDark: Boolean,
    val colors: Map<String, String>
)
