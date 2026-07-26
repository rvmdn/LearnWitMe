package com.learnwitme.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun MetaTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val currentTheme by themeManager.currentTheme.collectAsState()
    val colorScheme = themeManager.getColorScheme(currentTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
