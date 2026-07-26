package com.learnwitme.core.config

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.core.storage.EncryptedDataStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ConfigManagerTest {

    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()
    private val encryptedDataStore = mockk<EncryptedDataStore>()

    private val testJson = """
        {
          "environment": {
            "name": "development",
            "debugEnabled": true,
            "sslPinningEnabled": false
          },
          "featureFlags": {
            "ai_features": true,
            "ocr_features": false
          },
          "navigation": {
            "defaultRoute": "home",
            "screens": [
              {"route": "home", "className": "HomeScreen", "title": "Home"}
            ]
          },
          "menus": [
            {"id": "menu_home", "title": "Home", "iconName": "home", "route": "home", "order": 1}
          ],
          "plugins": {},
          "aiProviders": [],
          "billingProducts": [],
          "apiEndpoints": {
            "auth_base": "https://auth.learnwitme.com/v1"
          }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.open("metadata/config.json") } returns ByteArrayInputStream(testJson.toByteArray())
        every { encryptedDataStore.getStringFlow(any(), any()) } returns flowOf("")
    }

    @Test
    fun testLoadConfig_success() {
        val manager = ConfigManager(context, encryptedDataStore)
        assertEquals("development", manager.config.environment.name)
        assertTrue(manager.isFeatureEnabled("ai_features"))
        assertEquals("https://auth.learnwitme.com/v1", manager.getApiEndpoint("auth_base"))
    }
}
