package com.learnwitme.core.config

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.core.storage.EncryptedDataStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class LocalizationManagerTest {

    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()
    private val encryptedDataStore = mockk<EncryptedDataStore>()
    private val configManager = mockk<ConfigManager>()

    private val idJson = """
        {
          "languageCode": "id",
          "languageName": "Bahasa Indonesia",
          "strings": {
            "app_name": "LearnWitMe",
            "nav_home": "Beranda",
            "nav_profile": "Profil"
          }
        }
    """.trimIndent()

    private val enJson = """
        {
          "languageCode": "en",
          "languageName": "English",
          "strings": {
            "app_name": "LearnWitMe",
            "nav_home": "Home",
            "nav_profile": "Profile"
          }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        every { context.assets } returns assetManager
        every { assetManager.open("metadata/i18n/id.json") } returns ByteArrayInputStream(idJson.toByteArray())
        every { assetManager.open("metadata/i18n/en.json") } returns ByteArrayInputStream(enJson.toByteArray())
        every { encryptedDataStore.getStringFlow("user_language_preference", "id") } returns flowOf("id")
        every { configManager.config } returns AppConfig(
            languageConfig = LanguageConfig(
                defaultLanguage = "id",
                supportedLanguages = listOf(
                    LanguageInfo("id", "Bahasa Indonesia", isDefault = true),
                    LanguageInfo("en", "English", isDefault = false)
                )
            )
        )
    }

    @Test
    fun testLoadDictionary_id_returnsIndonesianStrings() {
        val manager = LocalizationManager(context, configManager, encryptedDataStore)
        manager.loadDictionary("id")

        assertEquals("Beranda", manager.getString("nav_home"))
        assertEquals("Profil", manager.getString("nav_profile"))
        assertEquals("FallbackText", manager.getString("non_existent_key", "FallbackText"))
    }

    @Test
    fun testLoadDictionary_en_returnsEnglishStrings() {
        val manager = LocalizationManager(context, configManager, encryptedDataStore)
        manager.loadDictionary("en")

        assertEquals("Home", manager.getString("nav_home"))
        assertEquals("Profile", manager.getString("nav_profile"))
    }
}
