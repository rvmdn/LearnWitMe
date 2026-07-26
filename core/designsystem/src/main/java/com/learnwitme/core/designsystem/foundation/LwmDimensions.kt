package com.learnwitme.core.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LearnWitMe Design System (LWM DS v1.0) - Dimension Tokens
 */
@Immutable
data class LwmDimensions(
    val minTouchTarget: Dp,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconExtraLarge: Dp,
    val borderThin: Dp,
    val borderThick: Dp,
    val buttonHeightMedium: Dp,
    val buttonHeightLarge: Dp,
    val bottomNavHeight: Dp,
    val topAppBarHeight: Dp
)

val DefaultLwmDimensions = LwmDimensions(
    minTouchTarget = 48.dp,
    iconSmall = 16.dp,
    iconMedium = 20.dp,
    iconLarge = 24.dp,
    iconExtraLarge = 32.dp,
    borderThin = 1.dp,
    borderThick = 2.dp,
    buttonHeightMedium = 40.dp,
    buttonHeightLarge = 48.dp,
    bottomNavHeight = 64.dp,
    topAppBarHeight = 56.dp
)

val LocalLwmDimensions = staticCompositionLocalOf { DefaultLwmDimensions }
