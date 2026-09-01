package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.storage.local.LearnWitMeDatabase
import com.learnwitme.core.storage.local.dao.KnowledgeCardDao
import com.learnwitme.core.storage.local.dao.KnowledgePackageDao
import com.learnwitme.core.storage.local.entity.KnowledgeCardEntity
import com.learnwitme.core.storage.local.entity.KnowledgePackageEntity
import com.learnwitme.core.storage.source.LearnWitMeLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgePackageRepositoryIntegrationTest {

    private val db = mockk<LearnWitMeDatabase>()
    private val packageDao = mockk<KnowledgePackageDao>(relaxed = true)
    private val cardDao = mockk<KnowledgeCardDao>(relaxed = true)
    private val localDataSource = LearnWitMeLocalDataSource(db)
    private val repository = KnowledgePackageRepositoryImpl(localDataSource)

    init {
        coEvery { db.knowledgePackageDao() } returns packageDao
        coEvery { db.knowledgeCardDao() } returns cardDao
    }

    @Test
    fun testSaveAndGetActivePackages_returnsConsistentCards() = runBlocking {
        val packageId = KnowledgePackageId("pkg_default_id")
        val cardId1 = KnowledgeCardId("c_kotlin_basics")
        val card1 = KnowledgeCard(
            id = cardId1,
            packageId = packageId,
            content = KnowledgeContent("Title 1", "Summary 1", "Why 1", "Example 1", "Mistake 1", "Not 1", "Best 1")
        )
        val pkg = KnowledgePackage(
            id = packageId,
            title = "Kurikulum Dasar Kotlin",
            description = "Dasar Kotlin",
            version = 1,
            cards = listOf(card1)
        )

        // 1. Test saveKnowledgePackage
        repository.savePackage(pkg)

        val insertedCardsSlot = slot<List<KnowledgeCardEntity>>()
        coVerify { cardDao.insertAll(capture(insertedCardsSlot)) }
        assertEquals(1, insertedCardsSlot.captured.size)
        assertEquals("pkg_default_id", insertedCardsSlot.captured.first().packageId)
        assertEquals("c_kotlin_basics", insertedCardsSlot.captured.first().id)

        // 2. Mock DAO query responses for getActivePackages()
        val pkgEntity = KnowledgePackageEntity(
            id = "pkg_default_id",
            title = "Kurikulum Dasar Kotlin",
            description = "Dasar Kotlin",
            version = "1",
            status = "PUBLISHED",
            isCore = false
        )
        val cardEntity = KnowledgeCardEntity(
            id = "c_kotlin_basics",
            packageId = "pkg_default_id",
            title = "Title 1",
            summary = "Summary 1",
            whyItMatters = "Why 1",
            realWorldExample = "Example 1",
            commonMistakes = "Mistake 1",
            whenNotToUse = "Not 1",
            bestPractice = "Best 1",
            referencesJson = "[]",
            officialDocumentationLink = "",
            difficulty = "BEGINNER",
            estimatedReadingTimeSeconds = 120,
            careerImpact = "HIGH",
            skillsJson = "[]",
            tagsJson = "[]",
            prerequisiteCardIdsJson = "[]",
            relatedTopicCodesJson = "[]"
        )

        coEvery { packageDao.getAllPackages() } returns listOf(pkgEntity)
        coEvery { cardDao.findByPackage("pkg_default_id") } returns listOf(cardEntity)

        // 3. Test getActivePackages()
        val activePackages = repository.getActivePackages()

        assertEquals(1, activePackages.size)
        val retrievedPkg = activePackages.first()
        assertEquals(packageId, retrievedPkg.id)
        assertTrue("Cards in active package must not be empty", retrievedPkg.cards.isNotEmpty())
        assertEquals(1, retrievedPkg.cards.size)
        assertEquals(cardId1, retrievedPkg.cards.first().id)
        assertEquals(packageId, retrievedPkg.cards.first().packageId)

        coVerify { cardDao.findByPackage("pkg_default_id") }
    }
}
