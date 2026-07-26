package com.learnwitme.core.theme.engine

import javax.inject.Inject

class ThemeValidator @Inject constructor() {
    fun validate(dto: ThemeBundleDto): Boolean {
        require(dto.manifest.id.isNotBlank()) { "Theme ID cannot be blank" }
        require(dto.manifest.name.isNotBlank()) { "Theme Name cannot be blank" }
        require(dto.manifest.version.isNotBlank()) { "Theme Version cannot be blank" }

        validateColorPalette(dto.colors.light)
        validateColorPalette(dto.colors.dark)

        require(dto.spacing.sDp >= 0) { "Spacing sDp cannot be negative" }
        require(dto.shape.smallDp >= 0) { "Shape smallDp cannot be negative" }
        require(dto.elevation.level0Dp >= 0) { "Elevation level0Dp cannot be negative" }

        return true
    }

    private fun validateColorPalette(palette: ColorPaletteDto) {
        val hexRegex = Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        require(palette.primary.matches(hexRegex)) { "Invalid primary color hex: ${palette.primary}" }
        require(palette.secondary.matches(hexRegex)) { "Invalid secondary color hex: ${palette.secondary}" }
        require(palette.background.matches(hexRegex)) { "Invalid background color hex: ${palette.background}" }
        require(palette.surface.matches(hexRegex)) { "Invalid surface color hex: ${palette.surface}" }
    }
}
