package com.learnwitme.feature.knowledgecard

import com.learnwitme.core.application.ai.feedback.AILearningInteractionResult
import com.learnwitme.core.application.ai.feedback.AILearningInteractionService
import com.learnwitme.core.application.ai.result.GeneratedExplanation
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.RoadmapRepository
import com.learnwitme.core.domain.service.Clock
import com.learnwitme.core.domain.usecase.CompleteLearningSessionWorkflow
import com.learnwitme.core.domain.usecase.FinishReviewWorkflow
import com.learnwitme.core.domain.usecase.LoadKnowledgeCardWorkflow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KnowledgeCardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val loadWorkflow = mockk<LoadKnowledgeCardWorkflow>()
    private val finishReviewWorkflow = mockk<FinishReviewWorkflow>(relaxed = true)
    private val completeLearningSessionWorkflow = mockk<CompleteLearningSessionWorkflow>(relaxed = true)
    private val roadmapRepository = mockk<RoadmapRepository>()
    private val clock = mockk<Clock>()
    private val aiService = mockk<AILearningInteractionService>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clock.currentTimeMillis() } returns 1000000L
        coEvery { roadmapRepository.getActiveRoadmapByUserId(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): KnowledgeCardViewModel {
        return KnowledgeCardViewModel(
            loadWorkflow,
            finishReviewWorkflow,
            completeLearningSessionWorkflow,
            roadmapRepository,
            clock,
            aiService
        )
    }

    @Test
    fun testLoadCardSuccess() = runTest(testDispatcher) {
        val cardId = KnowledgeCardId("c1")
        val card = KnowledgeCard(
            cardId, KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        coEvery { loadWorkflow.execute(cardId) } returns card

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard("c1"))

        assertTrue(viewModel.uiState.value is KnowledgeCardState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is KnowledgeCardState.Success)
        val state = viewModel.uiState.value as KnowledgeCardState.Success
        assertEquals(card, state.card)
        assertEquals(AiAssistanceState.Idle, state.aiState)
    }

    @Test
    fun testLoadCardNotFound() = runTest(testDispatcher) {
        val cardId = KnowledgeCardId("c_unknown")
        coEvery { loadWorkflow.execute(cardId) } returns null

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard("c_unknown"))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is KnowledgeCardState.Error)
        assertEquals("Card not found: c_unknown", (viewModel.uiState.value as KnowledgeCardState.Error).message)
    }

    @Test
    fun testSubmitReview_withActiveRoadmapUnit_completesSession() = runTest(testDispatcher) {
        val cardId = "c1"
        val card = KnowledgeCard(
            KnowledgeCardId(cardId), KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        val unit = LearningUnit(
            id = LearningUnitId("u1"),
            title = "Unit 1",
            cardId = KnowledgeCardId("c1"),
            quizId = QuizId("q1"),
            estimatedMinutes = 10,
            topicCode = "tp_kotlin"
        )
        val roadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_default"),
            goal = LearningGoal("Kotlin Dev", "cp_kotlin_dev", DifficultyLevel.MID),
            units = listOf(unit),
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = emptySet()
        )
        coEvery { loadWorkflow.execute(KnowledgeCardId(cardId)) } returns card
        coEvery { roadmapRepository.getActiveRoadmapByUserId(any()) } returns roadmap

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnSubmitReview(cardId, 0.8))
        advanceUntilIdle()

        coVerify { finishReviewWorkflow.execute(any(), KnowledgeCardId(cardId), any(), any()) }
        coVerify { completeLearningSessionWorkflow.execute(any(), roadmap.id, any(), any()) }
    }

    @Test
    fun testRequestAiExplanation_success_updatesAiStateToSuccess() = runTest(testDispatcher) {
        val cardId = "c1"
        val card = KnowledgeCard(
            KnowledgeCardId(cardId), KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        val explanationResult = mockk<AILearningInteractionResult<GeneratedExplanation>>(relaxed = true)
        every { explanationResult.generatedResult.content } returns "AI explanation for Kotlin null safety"

        coEvery { loadWorkflow.execute(KnowledgeCardId(cardId)) } returns card
        coEvery { aiService.executeExplanation(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns explanationResult

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnRequestAiExplanation(cardId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is KnowledgeCardState.Success)
        val state = viewModel.uiState.value as KnowledgeCardState.Success
        assertTrue(state.aiState is AiAssistanceState.Success)
        val aiSuccess = state.aiState as AiAssistanceState.Success
        assertEquals("AI explanation for Kotlin null safety", aiSuccess.explanation)
    }

    @Test
    fun testRequestAiExplanation_failure_updatesAiStateToError_preservesCardState() = runTest(testDispatcher) {
        val cardId = "c1"
        val card = KnowledgeCard(
            KnowledgeCardId(cardId), KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        coEvery { loadWorkflow.execute(KnowledgeCardId(cardId)) } returns card
        coEvery { aiService.executeExplanation(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws RuntimeException("AI service unavailable")

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnRequestAiExplanation(cardId))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is KnowledgeCardState.Success)
        val state = viewModel.uiState.value as KnowledgeCardState.Success
        assertEquals(card, state.card)
        assertTrue(state.aiState is AiAssistanceState.Error)
        val aiError = state.aiState as AiAssistanceState.Error
        assertEquals("AI service unavailable", aiError.message)
    }

    @Test
    fun testSubmitAiFeedback_recordsFeedback() = runTest(testDispatcher) {
        val cardId = "c1"
        val card = KnowledgeCard(
            KnowledgeCardId(cardId), KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        val explanationResult = mockk<AILearningInteractionResult<GeneratedExplanation>>(relaxed = true)
        every { explanationResult.generatedResult.content } returns "Explanation"

        coEvery { loadWorkflow.execute(KnowledgeCardId(cardId)) } returns card
        coEvery { aiService.executeExplanation(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns explanationResult

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnRequestAiExplanation(cardId))
        advanceUntilIdle()

        val aiSuccess = (viewModel.uiState.value as KnowledgeCardState.Success).aiState as AiAssistanceState.Success
        viewModel.onEvent(KnowledgeCardEvent.OnSubmitAiFeedback(aiSuccess.interactionId, 1.0))
        advanceUntilIdle()

        val updatedAiSuccess = (viewModel.uiState.value as KnowledgeCardState.Success).aiState as AiAssistanceState.Success
        assertTrue(updatedAiSuccess.feedbackSubmitted)
        coVerify(atLeast = 2) { aiService.executeExplanation(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun testDismissAiExplanation_resetsAiStateToIdle() = runTest(testDispatcher) {
        val cardId = "c1"
        val card = KnowledgeCard(
            KnowledgeCardId(cardId), KnowledgePackageId("pkg_1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best",
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact",
            emptyList(), emptyList(), listOf("tp_sql")
        )
        val explanationResult = mockk<AILearningInteractionResult<GeneratedExplanation>>(relaxed = true)
        every { explanationResult.generatedResult.content } returns "Explanation"

        coEvery { loadWorkflow.execute(KnowledgeCardId(cardId)) } returns card
        coEvery { aiService.executeExplanation(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns explanationResult

        val viewModel = createViewModel()
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnRequestAiExplanation(cardId))
        advanceUntilIdle()

        viewModel.onEvent(KnowledgeCardEvent.OnDismissAiExplanation)
        advanceUntilIdle()

        val state = viewModel.uiState.value as KnowledgeCardState.Success
        assertEquals(AiAssistanceState.Idle, state.aiState)
    }
}
