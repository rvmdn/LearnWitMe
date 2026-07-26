package com.learnwitme.core.domain.feed

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeLearningRepository : LearningRepository {
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

class DailyKnowledgeFeedManagerTest {

    private val repository = FakeLearningRepository()
    private val feedManager = DailyKnowledgeFeedManager(repository)
    private val userId = UserId("user_101")

    private fun createCard(idStr: String): KnowledgeCard {
        return KnowledgeCard(
            id = KnowledgeCardId(idStr),
            packageId = KnowledgePackageId("pkg_1"),
            content = KnowledgeContent("Title $idStr", "Summary", "Why", "Example", "Mistake", "Not", "Best")
        )
    }

    @Test
    fun testEmptyCardListReturnsEmptyResult() = runBlocking {
        val result = feedManager.generateDailyFeed(userId, emptyList(), currentTimeMs = 1000L)
        assertTrue(result.feedItems.isEmpty())
        assertEquals(0, result.reviewCount)
        assertEquals(0, result.newCardCount)
        assertEquals(0L, result.estimatedStudyTimeSeconds)
    }

    @Test
    fun testPriorityRulesAndOrdering() = runBlocking {
        val card1 = createCard("card_due_1")
        val card2 = createCard("card_active_1")
        val card3 = createCard("card_new_1")

        // card1 is overdue
        repository.reviewSchedulesMap[userId.value] = listOf(
            ReviewSchedule(card1.id, intervalDays = 1, nextReviewTimestamp = 500L, easeFactor = 2.5, repetitions = 1)
        )
        // card2 is active studying
        repository.userKnowledgeMap[userId.value] = UserKnowledge(
            id = userId,
            activeCardIds = setOf(card2.id)
        )

        val result = feedManager.generateDailyFeed(
            userId = userId,
            availableCards = listOf(card3, card2, card1),
            config = DailyFeedConfiguration(dailyCardLimit = 5, reviewWeight = 0.6, newCardWeight = 0.4),
            currentTimeMs = 1000L
        )

        assertEquals(3, result.feedItems.size)
        // Overdue review should come first
        assertEquals("card_due_1", result.feedItems[0].card.id.value)
        assertEquals(FeedReason.ReviewDue, result.feedItems[0].reason)

        // Active weak skill card second
        assertEquals("card_active_1", result.feedItems[1].card.id.value)
        assertEquals(FeedReason.WeakSkill, result.feedItems[1].reason)

        // New topic card third
        assertEquals("card_new_1", result.feedItems[2].card.id.value)
        assertEquals(FeedReason.NewTopic, result.feedItems[2].reason)
    }

    @Test
    fun testDeduplicationAndDailyLimit() = runBlocking {
        val card1 = createCard("c1")
        val card2 = createCard("c2")
        val card3 = createCard("c3")

        // Pass duplicates of card1
        val result = feedManager.generateDailyFeed(
            userId = userId,
            availableCards = listOf(card1, card1, card2, card3),
            config = DailyFeedConfiguration(dailyCardLimit = 2),
            currentTimeMs = 1000L
        )

        assertEquals(2, result.feedItems.size)
        val ids = result.feedItems.map { it.card.id.value }
        assertEquals(ids.distinct().size, ids.size)
    }

    @Test
    fun testDeterministicOutputForIdenticalInput() = runBlocking {
        val cardA = createCard("card_A")
        val cardB = createCard("card_B")
        val cardC = createCard("card_C")

        val available = listOf(cardC, cardA, cardB)

        val result1 = feedManager.generateDailyFeed(userId, available, currentTimeMs = 1000L)
        val result2 = feedManager.generateDailyFeed(userId, available, currentTimeMs = 1000L)

        assertEquals(result1.feedItems.map { it.card.id.value }, result2.feedItems.map { it.card.id.value })
    }

    @Test
    fun testAllCardsMasteredHandling() = runBlocking {
        val card1 = createCard("card_m1")
        repository.userKnowledgeMap[userId.value] = UserKnowledge(
            id = userId,
            masteredCardIds = setOf(card1.id)
        )

        val result = feedManager.generateDailyFeed(userId, listOf(card1), currentTimeMs = 1000L)

        assertEquals(1, result.feedItems.size)
        assertEquals(FeedReason.Recommended, result.feedItems.first().reason)
    }
}
