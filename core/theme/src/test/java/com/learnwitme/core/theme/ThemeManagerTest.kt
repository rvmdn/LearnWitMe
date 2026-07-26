package com.learnwitme.core.theme

import android.content.Context
import android.content.res.AssetManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ThemeManagerTest {

    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()

    private val testThemeJson = """
        {
          "name": "Forest",
          "isDark": false,
          "colors": {
            "primary": "#2E7D32",
            "onPrimary": "#FFFFFF",
            "background": "#F1F8E9",
            "onBackground": "#1B5E20"
          }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.list("themes") } returns arrayOf("Forest.json")
        every { assetManager.open("themes/Forest.json") } returns ByteArrayInputStream(testThemeJson.toByteArray())
    }

    @Test
    fun testLoadAvailableThemes_success() {
        val manager = ThemeManager(context)
        val themes = manager.themes.value
        assertEquals(1, themes.size)
        assertNotNull(themes["Forest"])
        assertEquals("Forest", manager.currentTheme.value?.name)
    }
}
