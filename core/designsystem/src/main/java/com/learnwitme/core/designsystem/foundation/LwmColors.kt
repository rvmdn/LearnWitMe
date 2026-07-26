package com.learnwitme.core.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * LearnWitMe Design System (LWM DS v1.0) - Color Tokens
 *
 * Designed with M3 Tech Slate & Emerald Focus palette:
 * - Neutral Slates (#0F172A, #1E293B, #64748B, #E2E8F0, #F8FAFC)
 * - Emerald Accent (#059669, #34D399) for Habit & Retention Focus
 * - Pure, high-contrast, professional engineering theme
 */
@Immutable
data class LwmColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val isDark: Boolean
)

val LightLwmColors = LwmColors(
    primary = Color(0xFF0F172A),          // Slate 900
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2E8F0), // Slate 200
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF059669),        // Emerald 600
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1FAE5),// Emerald 100
    onSecondaryContainer = Color(0xFF065F46),
    background = Color(0xFFF8FAFC),       // Slate 50
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),        // Slate 800
    surfaceVariant = Color(0xFFF1F5F9),   // Slate 100
    onSurfaceVariant = Color(0xFF64748B), // Slate 500
    outline = Color(0xFFE2E8F0),          // Slate 200
    outlineVariant = Color(0xFFCBD5E1),   // Slate 300
    error = Color(0xFFDC2626),            // Red 600
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    isDark = false
)

val DarkLwmColors = LwmColors(
    primary = Color(0xFFF8FAFC),          // Slate 50
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF334155), // Slate 700
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF34D399),        // Emerald 400
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF0F172A),       // Slate 900
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),          // Slate 800
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),   // Slate 700
    onSurfaceVariant = Color(0xFF94A3B8), // Slate 400
    outline = Color(0xFF334155),          // Slate 700
    outlineVariant = Color(0xFF475569),   // Slate 600
    error = Color(0xFFF87171),            // Red 400
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFECFDF5),
    isDark = true
)

val LocalLwmColors = staticCompositionLocalOf { LightLwmColors }
