package com.learnwitme.core.theme

import com.learnwitme.core.theme.engine.ColorPaletteDto
import com.learnwitme.core.theme.engine.ColorTokensDto
import com.learnwitme.core.theme.engine.ComponentTokensDto
import com.learnwitme.core.theme.engine.ElevationTokensDto
import com.learnwitme.core.theme.engine.IconTokensDto
import com.learnwitme.core.theme.engine.MotionTokensDto
import com.learnwitme.core.theme.engine.ShapeTokensDto
import com.learnwitme.core.theme.engine.SpacingTokensDto
import com.learnwitme.core.theme.engine.ThemeBundleDto
import com.learnwitme.core.theme.engine.ThemeEngine
import com.learnwitme.core.theme.engine.ThemeManifestDto
import com.learnwitme.core.theme.engine.ThemeRepository
import com.learnwitme.core.theme.engine.ThemeValidator
import com.learnwitme.core.theme.engine.TypographyTokensDto
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeEngineTest {

    private val validator = ThemeValidator()

    @Test
    fun testThemeValidationSuccess() {
        val sampleBundle = ThemeBundleDto(
            manifest = ThemeManifestDto(
                id = "default",
                name = "Default",
                version = "1.0.0",
                author = "LearnWitMe",
                supportsDark = true,
                minimumUiVersion = "1.0"
            ),
            colors = ColorTokensDto(
                light = ColorPaletteDto(
                    primary = "#FF0F172A",
                    onPrimary = "#FFFFFFFF",
                    primaryContainer = "#FFE2E8F0",
                    onPrimaryContainer = "#FF0F172A",
                    secondary = "#FF059669",
                    onSecondary = "#FFFFFFFF",
                    secondaryContainer = "#FFD1FAE5",
                    onSecondaryContainer = "#FF065F46",
                    background = "#FFF8FAFC",
                    onBackground = "#FF0F172A",
                    surface = "#FFFFFFFF",
                    onSurface = "#FF1E293B",
                    surfaceVariant = "#FFF1F5F9",
                    onSurfaceVariant = "#FF64748B",
                    outline = "#FFE2E8F0",
                    outlineVariant = "#FFCBD5E1",
                    error = "#FFDC2626",
                    onError = "#FFFFFFFF"
                ),
                dark = ColorPaletteDto(
                    primary = "#FFF8FAFC",
                    onPrimary = "#FF0F172A",
                    primaryContainer = "#FF334155",
                    onPrimaryContainer = "#FFF8FAFC",
                    secondary = "#FF34D399",
                    onSecondary = "#FF064E3B",
                    secondaryContainer = "#FF065F46",
                    onSecondaryContainer = "#FFA7F3D0",
                    background = "#FF0F172A",
                    onBackground = "#FFF8FAFC",
                    surface = "#FF1E293B",
                    onSurface = "#FFF8FAFC",
                    surfaceVariant = "#FF334155",
                    onSurfaceVariant = "#FF94A3B8",
                    outline = "#FF334155",
                    outlineVariant = "#FF475569",
                    error = "#FFF87171",
                    onError = "#FF7F1D1D"
                )
            ),
            typography = TypographyTokensDto(),
            spacing = SpacingTokensDto(),
            shape = ShapeTokensDto(),
            elevation = ElevationTokensDto(),
            motion = MotionTokensDto(),
            icons = IconTokensDto(),
            component = ComponentTokensDto()
        )

        val result = validator.validate(sampleBundle)
        assertTrue(result)
    }

    @Test
    fun testParseColorHex() {
        val repo = mockk<ThemeRepository>(relaxed = true)
        val engine = ThemeEngine(repo)
        val colorHex = engine.parseColorHex("#FF0F172A")
        assertEquals(0xFF0F172AL, colorHex)
    }
}
