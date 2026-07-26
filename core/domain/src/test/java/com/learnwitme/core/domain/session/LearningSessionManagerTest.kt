package com.learnwitme.core.domain.session

import com.learnwitme.core.domain.event.DailyFeedGeneratedEvent
import com.learnwitme.core.domain.event.ReviewCompletedEvent
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.usecase.CompleteReviewUseCase
import com.learnwitme.core.domain.usecase.DailyRecommendationResult
import com.learnwitme.core.domain.usecase.GenerateDailyRecommendationUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LearningSessionManagerTest {

    private val generateDailyRecommendationUseCase = mockk<GenerateDailyRecommendationUseCase>()
    private val completeReviewUseCase = mockk<CompleteReviewUseCase>()
    private lateinit var sessionManager: LearningSessionManager

    private val userId = UserId("user_test")
    private val card1 = KnowledgeCard(
        id = KnowledgeCardId("card_1"),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title 1", "Summary 1", "Why", "Example", "Mistake", "Not", "Best")
    )
    private val card2 = KnowledgeCard(
        id = KnowledgeCardId("card_2"),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title 2", "Summary 2", "Why", "Example", "Mistake", "Not", "Best")
    )

    @Before
    fun setUp() {
        sessionManager = LearningSessionManager(generateDailyRecommendationUseCase, completeReviewUseCase)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(LearningSessionState.Idle, sessionManager.state.value)
    }

    @Test
    fun testStartSessionTransitionsToStudying() = runBlocking {
        val availableCards = listOf(card1, card2)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"), Recommendation(card2.id, 0.8, "Next"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 2, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)

        val currentState = sessionManager.state.value
        assertTrue(currentState is LearningSessionState.Studying)
        val studying = currentState as LearningSessionState.Studying
        assertEquals("card_1", studying.currentCard.id.value)
        assertEquals(0, studying.cardIndex)
        assertEquals(2, studying.totalCards)
    }

    @Test
    fun testPauseAndResumeSessionTransitions() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)
        assertTrue(sessionManager.state.value is LearningSessionState.Studying)

        sessionManager.pauseSession()
        assertEquals(LearningSessionState.Paused, sessionManager.state.value)

        sessionManager.resumeSession()
        assertTrue(sessionManager.state.value is LearningSessionState.Studying)
    }

    @Test
    fun testSubmitAnswerAdvancesCardAndTracksMetrics() = runBlocking {
        val availableCards = listOf(card1, card2)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"), Recommendation(card2.id, 0.8, "Next"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 2, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)
        coEvery { completeReviewUseCase.execute(userId, any(), any(), any()) } returns ReviewCompletedEvent("rev_1", 1000L, userId.value, cardId = card1.id.value, qualityScore = 0.8, nextReviewTimestamp = 2000L)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.submitAnswer(userId, 0.8, 1000L)

        val currentState = sessionManager.state.value
        assertTrue(currentState is LearningSessionState.Studying)
        val studying = currentState as LearningSessionState.Studying
        assertEquals("card_2", studying.currentCard.id.value)
        assertEquals(1, studying.cardIndex)
    }

    @Test
    fun testSkipCardAdvancesCard() = runBlocking {
        val availableCards = listOf(card1, card2)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"), Recommendation(card2.id, 0.8, "Next"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 2, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.skipCard(1000L)

        val currentState = sessionManager.state.value
        assertTrue(currentState is LearningSessionState.Studying)
        val studying = currentState as LearningSessionState.Studying
        assertEquals("card_2", studying.currentCard.id.value)
    }

    @Test
    fun testFinishSessionCalculatesResults() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)
        coEvery { completeReviewUseCase.execute(userId, any(), any(), any()) } returns ReviewCompletedEvent("rev_1", 1000L, userId.value, cardId = card1.id.value, qualityScore = 0.9, nextReviewTimestamp = 2000L)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.submitAnswer(userId, 0.9, 11000L)

        val currentState = sessionManager.state.value
        assertTrue(currentState is LearningSessionState.Completed)
        val completed = currentState as LearningSessionState.Completed
        assertEquals(1, completed.result.cardsStudied)
        assertEquals(1, completed.result.correctAnswers)
        assertEquals(100.0, completed.result.accuracyPercentage, 0.01)
        assertEquals(10L, completed.result.timeSpentSeconds)
    }

    @Test
    fun testCancelSessionTransitionsToCancelled() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.cancelSession()

        assertEquals(LearningSessionState.Cancelled, sessionManager.state.value)
    }

    // Negative Test Cases

    @Test
    fun testCannotResumeWhenIdle() = runBlocking {
        sessionManager.resumeSession()
        assertEquals(LearningSessionState.Idle, sessionManager.state.value)
    }

    @Test
    fun testCannotPauseAfterCompleted() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)
        coEvery { completeReviewUseCase.execute(userId, any(), any(), any()) } returns ReviewCompletedEvent("rev_1", 1000L, userId.value, cardId = card1.id.value, qualityScore = 0.9, nextReviewTimestamp = 2000L)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.submitAnswer(userId, 0.9, 2000L)

        assertTrue(sessionManager.state.value is LearningSessionState.Completed)
        sessionManager.pauseSession()
        assertTrue(sessionManager.state.value is LearningSessionState.Completed)
    }

    @Test
    fun testCannotSubmitAnswerWhenCancelled() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.cancelSession()

        assertEquals(LearningSessionState.Cancelled, sessionManager.state.value)
        sessionManager.submitAnswer(userId, 0.8, 2000L)
        assertEquals(LearningSessionState.Cancelled, sessionManager.state.value)
    }

    @Test
    fun testCannotSkipCardWhenCompleted() = runBlocking {
        val availableCards = listOf(card1)
        val recs = listOf(Recommendation(card1.id, 0.9, "Focus"))
        val event = DailyFeedGeneratedEvent("evt_1", 1000L, userId.value, itemCount = 1, topFocusCardId = card1.id.value)

        coEvery { generateDailyRecommendationUseCase.execute(userId, availableCards, 1000L) } returns DailyRecommendationResult(recs, event)

        sessionManager.startSession(userId, availableCards, 1000L)
        sessionManager.finishSession(2000L)

        assertTrue(sessionManager.state.value is LearningSessionState.Completed)
        sessionManager.skipCard(3000L)
        assertTrue(sessionManager.state.value is LearningSessionState.Completed)
    }
}
