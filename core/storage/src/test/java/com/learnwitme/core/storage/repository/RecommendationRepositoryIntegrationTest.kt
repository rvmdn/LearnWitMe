package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.storage.local.dao.RecommendationDao
import com.learnwitme.core.storage.local.entity.RecommendationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecommendationRepositoryIntegrationTest {

    private val recommendationDao = mockk<RecommendationDao>(relaxed = true)
    private val repository = RecommendationRepositoryImpl(recommendationDao)

    @Test
    fun testSaveRecommendation() = runBlocking {
        val userId = UserId("user_101")
        val recommendation = Recommendation(KnowledgeCardId("card_101"), 0.95, "Due for review")

        repository.saveRecommendation(userId, recommendation)

        coVerify { recommendationDao.save(match { it.userId == "user_101" && it.cardId == "card_101" }) }
    }

    @Test
    fun testLoadRecommendation() = runBlocking {
        val userId = UserId("user_101")
        val entity = RecommendationEntity("user_101", "card_101", 0.95, "Due for review")

        coEvery { recommendationDao.findByUser("user_101") } returns listOf(entity)

        val result = repository.findRecommendation(userId)

        assertNotNull(result)
        assertEquals("card_101", result?.cardId?.value)
        assertEquals(0.95, result?.score ?: 0.0, 0.001)
    }
}
