package com.learnwitme.core.theme.engine

import kotlinx.serialization.json.Json
import javax.inject.Inject

class ThemeParser @Inject constructor() {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parseBundle(loader: ThemeLoader, themeId: String): ThemeBundleDto {
        val manifest = json.decodeFromString<ThemeManifestDto>(loader.loadRawAssetString(themeId, "manifest.json"))
        val colors = json.decodeFromString<ColorTokensDto>(loader.loadRawAssetString(themeId, "colors.json"))
        val typography = json.decodeFromString<TypographyTokensDto>(loader.loadRawAssetString(themeId, "typography.json"))
        val spacing = json.decodeFromString<SpacingTokensDto>(loader.loadRawAssetString(themeId, "spacing.json"))
        val shape = json.decodeFromString<ShapeTokensDto>(loader.loadRawAssetString(themeId, "shape.json"))
        val elevation = json.decodeFromString<ElevationTokensDto>(loader.loadRawAssetString(themeId, "elevation.json"))
        val motion = json.decodeFromString<MotionTokensDto>(loader.loadRawAssetString(themeId, "motion.json"))
        val icons = json.decodeFromString<IconTokensDto>(loader.loadRawAssetString(themeId, "icons.json"))
        val component = json.decodeFromString<ComponentTokensDto>(loader.loadRawAssetString(themeId, "component.json"))

        return ThemeBundleDto(
            manifest = manifest,
            colors = colors,
            typography = typography,
            spacing = spacing,
            shape = shape,
            elevation = elevation,
            motion = motion,
            icons = icons,
            component = component
        )
    }
}
