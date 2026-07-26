package com.learnwitme.core.domain.adaptive

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

class FakeAdaptiveLearningRepository : LearningRepository {
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

class AdaptiveDifficultyManagerTest {

    private val repository = FakeAdaptiveLearningRepository()
    private val manager = AdaptiveDifficultyManager(repository)
    private val userId = UserId("usr_mgr_1")

    @Test
    fun testEmptyProfileEvaluatesSafely() = runBlocking {
        val recommendation = manager.evaluateUserDifficulty(userId, currentTimeMs = 1000L)
        assertNotNull(recommendation)
        assertEquals(AdaptiveDifficultyLevel.BEGINNER, recommendation.targetLevel)
        assertEquals(DifficultyAdjustmentReason.CONSISTENT_PROGRESS, recommendation.reason)
    }

    @Test
    fun testMasteredProfileEvaluatesNextLevel() = runBlocking {
        val mastered = (1..10).map { KnowledgeCardId("c_$it") }.toSet()
        repository.userKnowledgeMap[userId.value] = UserKnowledge(id = userId, masteredCardIds = mastered)

        val recommendation = manager.evaluateUserDifficulty(userId, currentTimeMs = 1000L)
        assertNotNull(recommendation)
        assertEquals(AdaptiveDifficultyLevel.EXPERT, recommendation.targetLevel)
    }
}
