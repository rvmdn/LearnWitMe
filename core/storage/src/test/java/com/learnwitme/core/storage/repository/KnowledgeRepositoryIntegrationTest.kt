package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.storage.local.dao.KnowledgeCardDao
import com.learnwitme.core.storage.local.dao.KnowledgePackageDao
import com.learnwitme.core.storage.local.entity.KnowledgeCardEntity
import com.learnwitme.core.storage.local.entity.KnowledgePackageEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KnowledgeRepositoryIntegrationTest {

    private val packageDao = mockk<KnowledgePackageDao>(relaxed = true)
    private val cardDao = mockk<KnowledgeCardDao>(relaxed = true)
    private val repository = KnowledgeRepositoryImpl(packageDao, cardDao)

    @Test
    fun testImportPackage() = runBlocking {
        val packageId = KnowledgePackageId("pkg_101")
        val cardId = KnowledgeCardId("card_101")
        val card = KnowledgeCard(
            id = cardId,
            packageId = packageId,
            content = KnowledgeContent("Title 1", "Summary 1", "Why 1", "Example 1", "Mistake 1", "Not 1", "Best 1")
        )
        val pkg = KnowledgePackage(
            id = packageId,
            title = "Kotlin Coroutines Package",
            description = "Learn coroutines",
            version = 1,
            cards = listOf(card)
        )

        repository.importPackage(pkg)

        coVerify { packageDao.insert(any()) }
        coVerify { cardDao.insertAll(any()) }
    }

    @Test
    fun testPublishPackage() = runBlocking {
        val packageId = KnowledgePackageId("pkg_101")
        val entity = KnowledgePackageEntity("pkg_101", "Title", "Desc", "1", "", "DRAFT", false)

        coEvery { packageDao.findById("pkg_101") } returns entity

        repository.publishPackage(packageId)

        coVerify { packageDao.update(match { it.status == "PUBLISHED" }) }
    }

    @Test
    fun testFindCard() = runBlocking {
        val cardId = KnowledgeCardId("card_101")
        val cardEntity = KnowledgeCardEntity(
            id = "card_101",
            packageId = "pkg_101",
            title = "Coroutines Flows",
            summary = "Summary",
            whyItMatters = "Why",
            realWorldExample = "Example",
            commonMistakes = "Mistake",
            whenNotToUse = "Not",
            bestPractice = "Best",
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

        coEvery { cardDao.findById("card_101") } returns cardEntity

        val result = repository.findCard(cardId)

        assertNotNull(result)
        assertEquals("Coroutines Flows", result?.content?.title)
    }

    @Test
    fun testSearchBySkills() = runBlocking {
        val skill = Skill("sk_coroutines", "Coroutines")
        val cardEntity = KnowledgeCardEntity(
            id = "card_101",
            packageId = "pkg_101",
            title = "Coroutines Flows",
            summary = "Summary",
            whyItMatters = "Why",
            realWorldExample = "Example",
            commonMistakes = "Mistake",
            whenNotToUse = "Not",
            bestPractice = "Best",
            referencesJson = "[]",
            officialDocumentationLink = "",
            difficulty = "BEGINNER",
            estimatedReadingTimeSeconds = 120,
            careerImpact = "HIGH",
            skillsJson = "[{\"code\":\"sk_coroutines\",\"name\":\"Coroutines\",\"category\":\"GENERAL\"}]",
            tagsJson = "[]",
            prerequisiteCardIdsJson = "[]",
            relatedTopicCodesJson = "[]"
        )

        coEvery { cardDao.getAll() } returns listOf(cardEntity)

        val results = repository.searchBySkills(listOf(skill))

        assertEquals(1, results.size)
        assertEquals("sk_coroutines", results.first().skills.first().code)
    }
}
