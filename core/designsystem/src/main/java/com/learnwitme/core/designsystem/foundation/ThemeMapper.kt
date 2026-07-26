package com.learnwitme.core.designsystem.foundation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.learnwitme.core.theme.engine.ThemeBundleDto

object ThemeMapper {
    fun parseColorHex(hex: String): Color {
        val clean = hex.removePrefix("#")
        val longVal = if (clean.length == 6) {
            0xFF000000 or clean.toLong(16)
        } else if (clean.length == 8) {
            clean.toLong(16)
        } else {
            0xFF0F172A
        }
        return Color(longVal)
    }

    fun toLwmColors(dto: ThemeBundleDto, isDark: Boolean): LwmColors {
        val palette = if (isDark) dto.colors.dark else dto.colors.light
        return LwmColors(
            primary = parseColorHex(palette.primary),
            onPrimary = parseColorHex(palette.onPrimary),
            primaryContainer = parseColorHex(palette.primaryContainer),
            onPrimaryContainer = parseColorHex(palette.onPrimaryContainer),
            secondary = parseColorHex(palette.secondary),
            onSecondary = parseColorHex(palette.onSecondary),
            secondaryContainer = parseColorHex(palette.secondaryContainer),
            onSecondaryContainer = parseColorHex(palette.onSecondaryContainer),
            background = parseColorHex(palette.background),
            onBackground = parseColorHex(palette.onBackground),
            surface = parseColorHex(palette.surface),
            onSurface = parseColorHex(palette.onSurface),
            surfaceVariant = parseColorHex(palette.surfaceVariant),
            onSurfaceVariant = parseColorHex(palette.onSurfaceVariant),
            outline = parseColorHex(palette.outline),
            outlineVariant = parseColorHex(palette.outlineVariant),
            error = parseColorHex(palette.error),
            onError = parseColorHex(palette.onError),
            errorContainer = parseColorHex(palette.error).copy(alpha = 0.2f),
            onErrorContainer = parseColorHex(palette.error),
            isDark = isDark
        )
    }

    fun toLwmSpacing(dto: ThemeBundleDto): LwmSpacing {
        return LwmSpacing(
            xxs = dto.spacing.xxsDp.dp,
            xs = dto.spacing.xsDp.dp,
            s = dto.spacing.sDp.dp,
            m = dto.spacing.mDp.dp,
            l = dto.spacing.lDp.dp,
            xl = dto.spacing.xlDp.dp,
            xxl = dto.spacing.xxlDp.dp
        )
    }

    fun toLwmShapes(dto: ThemeBundleDto): LwmShapes {
        return LwmShapes(
            small = RoundedCornerShape(dto.shape.smallDp.dp),
            medium = RoundedCornerShape(dto.shape.mediumDp.dp),
            large = RoundedCornerShape(dto.shape.largeDp.dp),
            extraLarge = RoundedCornerShape(dto.shape.extraLargeDp.dp),
            full = RoundedCornerShape(dto.shape.fullDp.dp)
        )
    }

    fun toLwmElevation(dto: ThemeBundleDto): LwmElevation {
        return LwmElevation(
            level0 = dto.elevation.level0Dp.dp,
            level1 = dto.elevation.level1Dp.dp,
            level2 = dto.elevation.level2Dp.dp,
            level3 = dto.elevation.level3Dp.dp
        )
    }

    fun toLwmMotion(dto: ThemeBundleDto): LwmMotion {
        return DefaultLwmMotion.copy(
            durationShort = dto.motion.durationShortMs,
            durationMedium = dto.motion.durationMediumMs,
            durationLong = dto.motion.durationLongMs
        )
    }

    fun toLwmTypography(dto: ThemeBundleDto): LwmTypography {
        return DefaultLwmTypography
    }
}
