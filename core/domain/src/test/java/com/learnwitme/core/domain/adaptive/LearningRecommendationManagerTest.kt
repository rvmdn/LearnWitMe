package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningRecommendationManagerTest {

    private val repository = FakeAdaptiveLearningRepository()
    private val diffManager = AdaptiveDifficultyManager(repository)
    private val recManager = LearningRecommendationManager(repository, diffManager)
    private val userId = UserId("usr_rec_1")

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title $id", "Sum", "Why", "Ex", "Mis", "Not", "Best")
    )

    @Test
    fun testEndToEndRecommendationGeneration() = runBlocking {
        val card1 = createCard("c_due")
        val card2 = createCard("c_new")

        repository.reviewSchedulesMap[userId.value] = listOf(
            ReviewSchedule(card1.id, intervalDays = 1, nextReviewTimestamp = 500L, easeFactor = 2.5, repetitions = 1)
        )

        val recommendations = recManager.generateRecommendations(
            userId = userId,
            availableCards = listOf(card2, card1),
            currentTimeMs = 1000L
        )

        assertEquals(2, recommendations.size)
        assertEquals("c_due", recommendations[0].card.id.value)
        assertEquals(RecommendationPriority.CRITICAL, recommendations[0].priority)
    }

    @Test
    fun testDeterministicRecommendationList() = runBlocking {
        val cards = (1..5).map { createCard("card_$it") }
        val recs1 = recManager.generateRecommendations(userId, cards, 1000L)
        val recs2 = recManager.generateRecommendations(userId, cards, 1000L)

        assertEquals(recs1.map { it.card.id.value }, recs2.map { it.card.id.value })
    }
}
