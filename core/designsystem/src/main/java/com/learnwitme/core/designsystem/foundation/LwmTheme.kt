package com.learnwitme.core.designsystem.foundation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.learnwitme.core.theme.engine.ThemeBundleDto

/**
 * LearnWitMe Design System Theme Wrapper (LWM UI Platform v1.0)
 *
 * Provides single source of truth for LWM DS tokens across all UI components.
 * Supports dynamic data-driven rendering via ThemeBundleDto.
 */
@Composable
fun LwmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeBundle: ThemeBundleDto? = null,
    content: @Composable () -> Unit
) {
    val colors = if (themeBundle != null) {
        ThemeMapper.toLwmColors(themeBundle, darkTheme)
    } else {
        if (darkTheme) DarkLwmColors else LightLwmColors
    }

    val spacing = if (themeBundle != null) ThemeMapper.toLwmSpacing(themeBundle) else DefaultLwmSpacing
    val shapes = if (themeBundle != null) ThemeMapper.toLwmShapes(themeBundle) else DefaultLwmShapes
    val elevation = if (themeBundle != null) ThemeMapper.toLwmElevation(themeBundle) else DefaultLwmElevation
    val motion = if (themeBundle != null) ThemeMapper.toLwmMotion(themeBundle) else DefaultLwmMotion
    val typography = if (themeBundle != null) ThemeMapper.toLwmTypography(themeBundle) else DefaultLwmTypography

    val m3ColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            error = colors.error,
            onError = colors.onError
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            error = colors.error,
            onError = colors.onError
        )
    }

    CompositionLocalProvider(
        LocalLwmColors provides colors,
        LocalLwmTypography provides typography,
        LocalLwmShapes provides shapes,
        LocalLwmElevation provides elevation,
        LocalLwmSpacing provides spacing,
        LocalLwmDimensions provides DefaultLwmDimensions,
        LocalLwmMotion provides motion
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            content = content
        )
    }
}

object LwmTheme {
    val colors: LwmColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmColors.current

    val typography: LwmTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmTypography.current

    val shapes: LwmShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmShapes.current

    val elevation: LwmElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmElevation.current

    val spacing: LwmSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmSpacing.current

    val dimensions: LwmDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmDimensions.current

    val motion: LwmMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalLwmMotion.current
}
