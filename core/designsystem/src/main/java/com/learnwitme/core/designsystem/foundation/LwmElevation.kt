package com.learnwitme.core.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LearnWitMe Design System (LWM DS v1.0) - Elevation Tokens
 *
 * M3 Flat-first approach (0dp default for cards, subtle 2dp/4dp for floating elements).
 */
@Immutable
data class LwmElevation(
    val level0: Dp,
    val level1: Dp,
    val level2: Dp,
    val level3: Dp
)

val DefaultLwmElevation = LwmElevation(
    level0 = 0.dp,
    level1 = 2.dp,
    level2 = 4.dp,
    level3 = 8.dp
)

val LocalLwmElevation = staticCompositionLocalOf { DefaultLwmElevation }
