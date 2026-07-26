package com.learnwitme.core.domain.progress

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FakeProgressLearningRepository : LearningRepository {
    var userKnowledgeMap = mutableMapOf<String, UserKnowledge>()
    var reviewSchedulesMap = mutableMapOf<String, List<ReviewSchedule>>()

    override suspend fun findUserKnowledge(userId: UserId): UserKnowledge? = userKnowledgeMap[userId.value]
    override suspend fun saveUserKnowledge(userKnowledge: UserKnowledge) { userKnowledgeMap[userKnowledge.id.value] = userKnowledge }
    override suspend fun startLearning(userId: UserId, cardId: KnowledgeCardId) {}
    override suspend fun markCardMastered(userId: UserId, cardId: KnowledgeCardId) {}
    override suspend fun updateCompetency(userId: UserId, skill: Skill, competency: Competency) {}
    override suspend fun findReviewSchedule(userId: UserId, cardId: KnowledgeCardId): ReviewSchedule? = null
    override suspend fun findDueReviews(userId: UserId, currentTimeMs: Long): List<ReviewSchedule> = reviewSchedulesMap[userId.value] ?: emptyList()
    override suspend fun recordReview(userId: UserId, cardId: KnowledgeCardId, schedule: ReviewSchedule) {}
}

class LearningProgressManagerTest {

    private val repository = FakeProgressLearningRepository()
    private val progressManager = LearningProgressManager(repository)
    private val userId = UserId("user_progress_1")

    @Test
    fun testZeroCardsDivisionByZeroProtection() = runBlocking {
        val summary = progressManager.generateProgressSummary(userId, totalAvailableCardsCount = 0, currentTimeMs = 1000L)
        assertNotNull(summary)
        assertEquals(1, summary.currentLevel)
        assertEquals(0, summary.totalXP)
        assertEquals(0.0, summary.completionRatePercentage, 0.01)
        assertEquals(0.0, summary.masteryRatePercentage, 0.01)
        assertEquals(0L, summary.totalStudyTimeSeconds)
    }

    @Test
    fun testXpAndLevelCalculation() = runBlocking {
        // 5 active cards, 3 mastered cards = 8 total learned cards
        val active = (1..5).map { KnowledgeCardId("card_active_$it") }.toSet()
        val mastered = (1..3).map { KnowledgeCardId("card_mastered_$it") }.toSet()

        repository.userKnowledgeMap[userId.value] = UserKnowledge(
            id = userId,
            activeCardIds = active,
            masteredCardIds = mastered
        )

        val summary = progressManager.generateProgressSummary(userId, totalAvailableCardsCount = 20, currentTimeMs = 1000L)

        // totalLearned = 8
        // totalXP = (8 * 10) + (3 * 25) = 80 + 75 = 155
        // currentLevel = (155 / 100) + 1 = 2
        assertEquals(155, summary.totalXP)
        assertEquals(2, summary.currentLevel)
        assertEquals(40.0, summary.completionRatePercentage, 0.01) // 8 / 20 * 100
        assertEquals(37.5, summary.masteryRatePercentage, 0.01)    // 3 / 8 * 100
    }

    @Test
    fun testAllCardsMastered() = runBlocking {
        val mastered = (1..10).map { KnowledgeCardId("c_$it") }.toSet()
        repository.userKnowledgeMap[userId.value] = UserKnowledge(
            id = userId,
            activeCardIds = emptySet(),
            masteredCardIds = mastered
        )

        val summary = progressManager.generateProgressSummary(userId, totalAvailableCardsCount = 10, currentTimeMs = 1000L)

        assertEquals(100.0, summary.completionRatePercentage, 0.01)
        assertEquals(100.0, summary.masteryRatePercentage, 0.01)
    }

    @Test
    fun testDeterministicCalculationForIdenticalInput() = runBlocking {
        val active = setOf(KnowledgeCardId("c1"), KnowledgeCardId("c2"))
        repository.userKnowledgeMap[userId.value] = UserKnowledge(id = userId, activeCardIds = active)

        val summary1 = progressManager.generateProgressSummary(userId, totalAvailableCardsCount = 10, currentTimeMs = 1000L)
        val summary2 = progressManager.generateProgressSummary(userId, totalAvailableCardsCount = 10, currentTimeMs = 1000L)

        assertEquals(summary1, summary2)
    }
}
