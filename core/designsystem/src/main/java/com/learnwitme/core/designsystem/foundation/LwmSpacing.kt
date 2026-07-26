package com.learnwitme.core.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LearnWitMe Design System (LWM DS v1.0) - Spacing Tokens (4dp/8dp Grid)
 */
@Immutable
data class LwmSpacing(
    val xxs: Dp,
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp
)

val DefaultLwmSpacing = LwmSpacing(
    xxs = 2.dp,
    xs = 4.dp,
    s = 8.dp,
    m = 16.dp,
    l = 24.dp,
    xl = 32.dp,
    xxl = 48.dp
)

val LocalLwmSpacing = staticCompositionLocalOf { DefaultLwmSpacing }
