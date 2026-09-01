package com.learnwitme.app.seeder

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.core.domain.model.KnowledgePackageId
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class DefaultPackageParserTest {

    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()

    private fun setupAssets() {
        every { context.assets } returns assetManager
        val assetsDir = listOf(
            File("src/main/assets"),
            File("app/src/main/assets"),
            File("../app/src/main/assets")
        ).find { it.exists() && it.isDirectory } ?: File("src/main/assets")

        every { assetManager.open("default-package.json") } answers {
            FileInputStream(File(assetsDir, "default-package.json"))
        }
        every { assetManager.open("default-package-en.json") } throws Exception("Not found")
        every { assetManager.open("default-package-id.json") } answers {
            FileInputStream(File(assetsDir, "default-package-id.json"))
        }
    }

    @Test
    fun testParse_enAsset_setsCorrectPackageIdOnAllCards() {
        setupAssets()
        val manifest = DefaultPackageParser.parse(context, "en")
        
        assertEquals(KnowledgePackageId("pkg_default"), manifest.metadata.id)
        assertTrue("Cards must not be empty", manifest.cards.isNotEmpty())
        for (card in manifest.cards) {
            assertEquals(
                "Card ${card.id.value} must have packageId matching manifest ID",
                manifest.metadata.id,
                card.packageId
            )
        }
    }

    @Test
    fun testParse_idAsset_setsCorrectPackageIdOnAllCards() {
        setupAssets()
        val manifest = DefaultPackageParser.parse(context, "id")
        
        assertEquals(KnowledgePackageId("pkg_default_id"), manifest.metadata.id)
        assertTrue("Cards must not be empty", manifest.cards.isNotEmpty())
        for (card in manifest.cards) {
            assertEquals(
                "Card ${card.id.value} must have packageId matching manifest ID",
                manifest.metadata.id,
                card.packageId
            )
        }
    }
}
