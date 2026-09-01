package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.KnowledgePackageRepository
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.repository.QuizRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PackageValidatorTest {

    private val packageRepo = mockk<KnowledgePackageRepository>(relaxed = true)
    private val knowledgeRepo = mockk<KnowledgeRepository>(relaxed = true)
    private val quizRepo = mockk<QuizRepository>(relaxed = true)

    private val validator = PackageValidator(packageRepo, knowledgeRepo)
    private val importer = PackageImporter(packageRepo, knowledgeRepo, quizRepo, validator)

    private fun createValidManifest(): PackageManifest {
        val metadata = PackageMetadata(
            id = KnowledgePackageId("pkg_test"),
            title = "Test Package",
            description = "Test Description",
            version = PackageVersion(1, 0, 0),
            checksum = PackageChecksum("placeholder")
        )
        val card = KnowledgeCard(
            id = KnowledgeCardId("c_test_1"),
            packageId = KnowledgePackageId("pkg_test"),
            content = KnowledgeContent("Title 1", "Summary 1", "Why", "Example", "Mistakes", "WhenNot", "Best")
        )
        val unit = LearningUnit(
            id = LearningUnitId("u_test_1"),
            title = "Unit 1",
            cardId = KnowledgeCardId("c_test_1"),
            quizId = null,
            estimatedMinutes = 5,
            topicCode = "tp_test"
        )
        val topic = Topic(
            code = "tp_test",
            name = "Test Topic",
            description = "Test Topic Desc"
        )
        val manifestWithoutChecksum = PackageManifest(
            metadata = metadata,
            topics = listOf(topic),
            cards = listOf(card),
            units = listOf(unit),
            quizzes = emptyList(),
            dependencies = emptyList()
        )
        val correctChecksum = calculateChecksum(manifestWithoutChecksum)
        return manifestWithoutChecksum.copy(
            metadata = metadata.copy(checksum = correctChecksum)
        )
    }

    @Before
    fun setup() {
        coEvery { packageRepo.getPackage(any()) } returns null
        coEvery { knowledgeRepo.getCard(any()) } returns null
    }

    @Test
    fun testValidPackage_passesValidation() = runBlocking {
        val manifest = createValidManifest()
        val result = validator.validate(manifest)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testCorruptedChecksum_failsValidation() = runBlocking {
        val manifest = createValidManifest()
        val corruptedManifest = manifest.copy(
            metadata = manifest.metadata.copy(checksum = PackageChecksum("invalid_checksum_hash"))
        )

        val result = validator.validate(corruptedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Invalid checksum") })
    }

    @Test
    fun testBlankMetadata_failsValidation() = runBlocking {
        val manifest = createValidManifest()
        val blankTitleManifest = manifest.copy(
            metadata = manifest.metadata.copy(title = "   ")
        )
        val correctChecksum = calculateChecksum(blankTitleManifest)
        val finalizedManifest = blankTitleManifest.copy(
            metadata = blankTitleManifest.metadata.copy(checksum = correctChecksum)
        )

        val result = validator.validate(finalizedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Package title cannot be blank") })
    }

    @Test
    fun testDuplicateCardIds_failsValidation() = runBlocking {
        val manifest = createValidManifest()
        val card2 = manifest.cards.first() // Duplicate ID
        val duplicateManifest = manifest.copy(cards = listOf(manifest.cards.first(), card2))
        val correctChecksum = calculateChecksum(duplicateManifest)
        val finalizedManifest = duplicateManifest.copy(
            metadata = duplicateManifest.metadata.copy(checksum = correctChecksum)
        )

        val result = validator.validate(finalizedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate KnowledgeCard IDs") })
    }

    @Test
    fun testMissingPackageDependency_failsValidation() = runBlocking {
        val manifest = createValidManifest()
        val withDep = manifest.copy(
            dependencies = listOf(KnowledgePackageId("pkg_missing_dependency"))
        )
        val correctChecksum = calculateChecksum(withDep)
        val finalizedManifest = withDep.copy(
            metadata = withDep.metadata.copy(checksum = correctChecksum)
        )

        val result = validator.validate(finalizedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Missing required package dependency") })
    }

    @Test
    fun testUnitReferencingNonExistentCard_failsValidation() = runBlocking {
        val manifest = createValidManifest()
        val orphanUnit = LearningUnit(
            id = LearningUnitId("u_orphan"),
            title = "Orphan Unit",
            cardId = KnowledgeCardId("c_non_existent"),
            quizId = null,
            estimatedMinutes = 5,
            topicCode = "tp_test"
        )
        val invalidManifest = manifest.copy(units = listOf(orphanUnit))
        val correctChecksum = calculateChecksum(invalidManifest)
        val finalizedManifest = invalidManifest.copy(
            metadata = invalidManifest.metadata.copy(checksum = correctChecksum)
        )

        val result = validator.validate(finalizedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("references card c_non_existent which is not in this package") })
    }

    @Test
    fun testCircularDependency_failsValidation() = runBlocking {
        val cardA = KnowledgeCard(
            id = KnowledgeCardId("c_a"),
            packageId = KnowledgePackageId("pkg_test"),
            content = KnowledgeContent("Card A", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best"),
            prerequisiteCardIds = listOf(KnowledgeCardId("c_b"))
        )
        val cardB = KnowledgeCard(
            id = KnowledgeCardId("c_b"),
            packageId = KnowledgePackageId("pkg_test"),
            content = KnowledgeContent("Card B", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best"),
            prerequisiteCardIds = listOf(KnowledgeCardId("c_a"))
        )
        val manifest = createValidManifest().copy(cards = listOf(cardA, cardB), units = emptyList())
        val correctChecksum = calculateChecksum(manifest)
        val finalizedManifest = manifest.copy(
            metadata = manifest.metadata.copy(checksum = correctChecksum)
        )

        val result = validator.validate(finalizedManifest)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Circular dependency detected") })
    }

    @Test
    fun testCorruptedPackage_failsBeforeWriteInImporter() = runBlocking {
        val manifest = createValidManifest().copy(
            metadata = createValidManifest().metadata.copy(checksum = PackageChecksum("corrupted_checksum"))
        )

        try {
            importer.import(manifest)
            fail("Expected IllegalArgumentException on corrupted package import")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Package validation failed"))
        }

        // Verify no save occurred
        coVerify(exactly = 0) { packageRepo.savePackage(any()) }
        coVerify(exactly = 0) { knowledgeRepo.saveCard(any()) }
    }
}
