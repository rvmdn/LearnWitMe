package com.learnwitme.core.theme.engine

import android.content.Context
import java.io.InputStream
import javax.inject.Inject

class ThemeLoader @Inject constructor(
    private val context: Context
) {
    fun listInstalledThemeIds(): List<String> {
        return try {
            context.assets.list("themes")?.toList() ?: listOf("default")
        } catch (e: Exception) {
            listOf("default")
        }
    }

    fun loadRawAssetString(themeId: String, fileName: String): String {
        val path = "themes/$themeId/$fileName"
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
