package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.KnowledgePackageRepository
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.repository.QuizRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageImporterTest {

    private val packageRepo = mockk<KnowledgePackageRepository>(relaxed = true)
    private val knowledgeRepo = mockk<KnowledgeRepository>(relaxed = true)
    private val quizRepo = mockk<QuizRepository>(relaxed = true)
    private val validator = mockk<PackageValidator>()

    private val importer = PackageImporter(packageRepo, knowledgeRepo, quizRepo, validator)

    @Test
    fun testImportPackage_withCustomPackageId_maintainsParentChildIntegrity() = runBlocking {
        val customPackageId = KnowledgePackageId("pkg_default_id")
        val metadata = PackageMetadata(
            id = customPackageId,
            title = "Kurikulum Dasar Kotlin",
            description = "Dasar Kotlin",
            version = PackageVersion(1, 0, 0),
            checksum = PackageChecksum("valid_checksum")
        )
        // Even if card initially had a different packageId (e.g. legacy pkg_default)
        val initialCard = KnowledgeCard(
            id = KnowledgeCardId("c_kotlin_basics"),
            packageId = KnowledgePackageId("pkg_default"),
            content = KnowledgeContent("Sintaks Dasar", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best")
        )
        val manifest = PackageManifest(
            metadata = metadata,
            topics = listOf(Topic("tp_kotlin", "Dasar Kotlin", "Desc")),
            cards = listOf(initialCard),
            units = listOf(LearningUnit(LearningUnitId("u1"), "U1", KnowledgeCardId("c_kotlin_basics"), null, 5, "tp_kotlin")),
            quizzes = emptyList(),
            dependencies = emptyList()
        )

        coEvery { validator.validate(manifest) } returns ValidationResult(isValid = true)

        val summary = importer.import(manifest)

        assertEquals("SUCCESS", summary.status)
        assertEquals(customPackageId, summary.packageId)

        // Capture package saved to packageRepository
        val pkgSlot = slot<KnowledgePackage>()
        coVerify { packageRepo.savePackage(capture(pkgSlot)) }
        val savedPkg = pkgSlot.captured
        assertEquals(customPackageId, savedPkg.id)
        assertEquals(1, savedPkg.cards.size)
        assertEquals(customPackageId, savedPkg.cards.first().packageId)
        assertNotEquals(KnowledgePackageId("pkg_default"), savedPkg.cards.first().packageId)

        // Capture card saved to knowledgeRepository
        val cardSlot = slot<KnowledgeCard>()
        coVerify { knowledgeRepo.saveCard(capture(cardSlot)) }
        val savedCard = cardSlot.captured
        assertEquals(customPackageId, savedCard.packageId)
        assertNotEquals(KnowledgePackageId("pkg_default"), savedCard.packageId)
    }
}
