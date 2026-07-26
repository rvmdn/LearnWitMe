package com.learnwitme.core.theme.engine

import kotlinx.serialization.Serializable

@Serializable
data class ThemeManifestDto(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val supportsDark: Boolean,
    val minimumUiVersion: String
)

@Serializable
data class ColorPaletteDto(
    val primary: String,
    val onPrimary: String,
    val primaryContainer: String,
    val onPrimaryContainer: String,
    val secondary: String,
    val onSecondary: String,
    val secondaryContainer: String,
    val onSecondaryContainer: String,
    val background: String,
    val onBackground: String,
    val surface: String,
    val onSurface: String,
    val surfaceVariant: String,
    val onSurfaceVariant: String,
    val outline: String,
    val outlineVariant: String,
    val error: String,
    val onError: String
)

@Serializable
data class ColorTokensDto(
    val light: ColorPaletteDto,
    val dark: ColorPaletteDto
)

@Serializable
data class TypographyTokensDto(
    val fontFamily: String = "default",
    val codeFontFamily: String = "monospace",
    val displayLargeSp: Int = 44,
    val displayMediumSp: Int = 36,
    val displaySmallSp: Int = 28,
    val headlineLargeSp: Int = 26,
    val headlineMediumSp: Int = 22,
    val headlineSmallSp: Int = 18,
    val titleLargeSp: Int = 18,
    val titleMediumSp: Int = 16,
    val titleSmallSp: Int = 14,
    val bodyLargeSp: Int = 16,
    val bodyMediumSp: Int = 14,
    val bodySmallSp: Int = 12,
    val labelLargeSp: Int = 14,
    val labelMediumSp: Int = 12,
    val labelSmallSp: Int = 11,
    val codeLargeSp: Int = 15,
    val codeMediumSp: Int = 13,
    val codeSmallSp: Int = 11
)

@Serializable
data class SpacingTokensDto(
    val xxsDp: Int = 2,
    val xsDp: Int = 4,
    val sDp: Int = 8,
    val mDp: Int = 16,
    val lDp: Int = 24,
    val xlDp: Int = 32,
    val xxlDp: Int = 48
)

@Serializable
data class ShapeTokensDto(
    val smallDp: Int = 8,
    val mediumDp: Int = 12,
    val largeDp: Int = 16,
    val extraLargeDp: Int = 24,
    val fullDp: Int = 100
)

@Serializable
data class ElevationTokensDto(
    val level0Dp: Int = 0,
    val level1Dp: Int = 2,
    val level2Dp: Int = 4,
    val level3Dp: Int = 8
)

@Serializable
data class MotionTokensDto(
    val durationShortMs: Int = 150,
    val durationMediumMs: Int = 300,
    val durationLongMs: Int = 500,
    val standardEasing: String = "fast_out_slow_in",
    val emphasizedEasing: String = "linear_out_slow_in"
)

@Serializable
data class IconTokensDto(
    val style: String = "outlined"
)

@Serializable
data class CodeBlockConfigDto(
    val theme: String = "dark",
    val showLineNumbers: Boolean = true,
    val allowCopy: Boolean = true
)

@Serializable
data class ComponentTokensDto(
    val buttonVariant: String = "filled",
    val cardVariant: String = "flat",
    val navigationVariant: String = "filled",
    val codeBlock: CodeBlockConfigDto = CodeBlockConfigDto()
)

data class ThemeBundleDto(
    val manifest: ThemeManifestDto,
    val colors: ColorTokensDto,
    val typography: TypographyTokensDto,
    val spacing: SpacingTokensDto,
    val shape: ShapeTokensDto,
    val elevation: ElevationTokensDto,
    val motion: MotionTokensDto,
    val icons: IconTokensDto,
    val component: ComponentTokensDto
)
