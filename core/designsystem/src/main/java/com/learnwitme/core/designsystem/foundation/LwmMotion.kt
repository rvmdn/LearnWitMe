package com.learnwitme.core.designsystem.foundation

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * LearnWitMe Design System (LWM DS v1.0) - Motion Tokens
 */
@Immutable
data class LwmMotion(
    val durationShort: Int,
    val durationMedium: Int,
    val durationLong: Int,
    val easingStandard: Easing,
    val easingEmphasized: Easing
)

val DefaultLwmMotion = LwmMotion(
    durationShort = 150,
    durationMedium = 300,
    durationLong = 500,
    easingStandard = FastOutSlowInEasing,
    easingEmphasized = LinearOutSlowInEasing
)

val LocalLwmMotion = staticCompositionLocalOf { DefaultLwmMotion }
