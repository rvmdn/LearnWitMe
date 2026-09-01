package com.learnwitme.app.seeder

import com.learnwitme.core.domain.model.DifficultyLevel
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class KnowledgeSeedValidationTest {

    private val enAssetFile = File("src/main/assets/default-package.json")
    private val idAssetFile = File("src/main/assets/default-package-id.json")

    @Test
    fun testEnglishAsset_parsesAndValidates() {
        assertTrue("EN asset file must exist", enAssetFile.exists())
        val json = enAssetFile.readText()
        val manifest = DefaultPackageParser.parse(json)

        assertNotNull(manifest)
        assertEquals("pkg_default", manifest.metadata.id.value)
        assertTrue("Must have at least 20 cards", manifest.cards.size >= 20)
        assertTrue("Must have at least 3 topics", manifest.topics.size >= 3)

        // Validate package/card invariant
        manifest.cards.forEach { card ->
            assertEquals("Card packageId must match metadata.id", manifest.metadata.id, card.packageId)
            assertTrue("Card title must not be blank", card.title.isNotBlank())
            assertTrue("Card summary must not be blank", card.summary.isNotBlank())
            assertTrue("Reading time must be positive", card.estimatedReadingTimeSeconds > 0)
        }

        // Validate unit card relations
        assertTrue("Units must not be empty", manifest.units.isNotEmpty())
        val cardIdSet = manifest.cards.map { it.id.value }.toSet()
        manifest.units.forEach { unit ->
            assertTrue("Unit cardId '${unit.cardId.value}' must exist in cards", cardIdSet.contains(unit.cardId.value))
        }

        // Validate quiz relations
        assertTrue("Quizzes must not be empty", manifest.quizzes.isNotEmpty())
        val quizIdSet = manifest.quizzes.map { it.id.value }.toSet()
        manifest.units.forEach { unit ->
            unit.quizId?.let { qId ->
                assertTrue("Quiz ID '${qId.value}' referenced by unit must exist", quizIdSet.contains(qId.value))
            }
        }

        // Validate prerequisite relations
        manifest.cards.forEach { card ->
            card.prerequisiteCardIds.forEach { prereqId ->
                assertTrue("Prerequisite '${prereqId.value}' must exist in package cards", cardIdSet.contains(prereqId.value))
            }
        }

        // Validate difficulty distribution
        val difficulties = manifest.cards.map { it.difficulty }.toSet()
        assertTrue("Must have BEGINNER difficulty", difficulties.contains(DifficultyLevel.BEGINNER))
        assertTrue("Must have JUNIOR difficulty", difficulties.contains(DifficultyLevel.JUNIOR))
        assertTrue("Must have MID difficulty", difficulties.contains(DifficultyLevel.MID))
        assertTrue("Must have SENIOR difficulty", difficulties.contains(DifficultyLevel.SENIOR))
    }

    @Test
    fun testIndonesianAsset_parsesAndValidates() {
        assertTrue("ID asset file must exist", idAssetFile.exists())
        val json = idAssetFile.readText()
        val manifest = DefaultPackageParser.parse(json)

        assertNotNull(manifest)
        assertEquals("pkg_default_id", manifest.metadata.id.value)
        assertTrue("Must have at least 20 cards", manifest.cards.size >= 20)
        assertTrue("Must have at least 3 topics", manifest.topics.size >= 3)

        // Validate package/card invariant
        manifest.cards.forEach { card ->
            assertEquals("Card packageId must match metadata.id", manifest.metadata.id, card.packageId)
            assertTrue("Card title must not be blank", card.title.isNotBlank())
            assertTrue("Card summary must not be blank", card.summary.isNotBlank())
        }

        // Validate unit card relations
        val cardIdSet = manifest.cards.map { it.id.value }.toSet()
        manifest.units.forEach { unit ->
            assertTrue("Unit cardId '${unit.cardId.value}' must exist in cards", cardIdSet.contains(unit.cardId.value))
        }

        // Validate quiz relations
        val quizIdSet = manifest.quizzes.map { it.id.value }.toSet()
        manifest.units.forEach { unit ->
            unit.quizId?.let { qId ->
                assertTrue("Quiz ID '${qId.value}' referenced by unit must exist", quizIdSet.contains(qId.value))
            }
        }
    }

    @Test
    fun testLocaleAssets_areStructurallyAligned() {
        val enJson = enAssetFile.readText()
        val idJson = idAssetFile.readText()

        val enManifest = DefaultPackageParser.parse(enJson)
        val idManifest = DefaultPackageParser.parse(idJson)

        assertEquals("Card count must match between locales", enManifest.cards.size, idManifest.cards.size)
        assertEquals("Unit count must match between locales", enManifest.units.size, idManifest.units.size)
        assertEquals("Quiz count must match between locales", enManifest.quizzes.size, idManifest.quizzes.size)

        val enCardIds = enManifest.cards.map { it.id.value }.sorted()
        val idCardIds = idManifest.cards.map { it.id.value }.sorted()
        assertEquals("Card IDs must be identical across locales", enCardIds, idCardIds)

        val enUnitIds = enManifest.units.map { it.id.value }.sorted()
        val idUnitIds = idManifest.units.map { it.id.value }.sorted()
        assertEquals("Unit IDs must be identical across locales", enUnitIds, idUnitIds)

        val enQuizIds = enManifest.quizzes.map { it.id.value }.sorted()
        val idQuizIds = idManifest.quizzes.map { it.id.value }.sorted()
        assertEquals("Quiz IDs must be identical across locales", enQuizIds, idQuizIds)
    }
}
