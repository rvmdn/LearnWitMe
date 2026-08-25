package com.learnwitme.feature.dailyfeed

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.DailyInitializationState
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.model.memory.personalization.PersonalizedContextResult
import com.learnwitme.core.domain.usecase.InitializePersonalizedDailyLearningWorkflow
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailyFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val workflow = mockk<InitializePersonalizedDailyLearningWorkflow>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoad_success_emitsSuccessState() = runTest(testDispatcher) {
        val decision = FeedDecision(
            feed = DailyFeed(emptyList()),
            layout = FeedLayout("default"),
            summary = FeedSummary(0)
        )
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        val successResult = DailyLearningInitializationResult.Success(
            userId = UserIdentity.Guest.DEFAULT_GUEST_ID,
            context = context,
            feedDecision = decision,
            initializationState = DailyInitializationState.NEW_GUEST_INITIALIZED
        )
        coEvery { workflow.execute(any()) } returns successResult

        val viewModel = DailyFeedViewModel(workflow)

        assertTrue(viewModel.uiState.value is DailyFeedState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyFeedState.Success)
        val state = viewModel.uiState.value as DailyFeedState.Success
        assertEquals(decision, state.feed)
        assertEquals(context, state.context)
        assertEquals(DailyInitializationState.NEW_GUEST_INITIALIZED, state.initializationState)
    }

    @Test
    fun testInitialLoad_incompletePersonalization_emitsIncompletePersonalizationState() = runTest(testDispatcher) {
        val incompleteReason = PersonalizedContextResult.Incomplete.EmptyPreferredTopics()
        val incompleteResult = DailyLearningInitializationResult.IncompletePersonalization(
            incompleteReason
        )
        coEvery { workflow.execute(any()) } returns incompleteResult

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyFeedState.IncompletePersonalization)
        val state = viewModel.uiState.value as DailyFeedState.IncompletePersonalization
        assertEquals(incompleteReason.message, state.reason)
    }

    @Test
    fun testInitialLoad_contentUnavailable_emitsContentUnavailableState() = runTest(testDispatcher) {
        val unavailableResult = DailyLearningInitializationResult.ContentUnavailable(
            userId = UserIdentity.Guest.DEFAULT_GUEST_ID,
            context = mockk(relaxed = true),
            reason = "No active roadmap found"
        )
        coEvery { workflow.execute(any()) } returns unavailableResult

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyFeedState.ContentUnavailable)
        val state = viewModel.uiState.value as DailyFeedState.ContentUnavailable
        assertEquals("No active roadmap found", state.reason)
    }

    @Test
    fun testInitialLoad_exception_emitsErrorState() = runTest(testDispatcher) {
        coEvery { workflow.execute(any()) } throws RuntimeException("Database error")

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyFeedState.Error)
        assertEquals("Database error", (viewModel.uiState.value as DailyFeedState.Error).message)
    }

    @Test
    fun testRefreshEvent_reloadsFeed() = runTest(testDispatcher) {
        val decision = FeedDecision(
            feed = DailyFeed(emptyList()),
            layout = FeedLayout("default"),
            summary = FeedSummary(0)
        )
        val successResult = DailyLearningInitializationResult.Success(
            userId = UserIdentity.Guest.DEFAULT_GUEST_ID,
            context = mockk(relaxed = true),
            feedDecision = decision,
            initializationState = DailyInitializationState.RETURNING_USER
        )
        coEvery { workflow.execute(any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            successResult
        }

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        viewModel.handleEvent(DailyFeedEvent.OnRefresh)
        runCurrent()
        assertTrue(viewModel.uiState.value is DailyFeedState.Loading)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DailyFeedState.Success)
    }

    @Test
    fun testOnStartCard_emitsNavigateToCardEffect() = runTest(testDispatcher) {
        val successResult = DailyLearningInitializationResult.Success(
            userId = UserIdentity.Guest.DEFAULT_GUEST_ID,
            context = mockk(relaxed = true),
            feedDecision = mockk(relaxed = true),
            initializationState = DailyInitializationState.NEW_GUEST_INITIALIZED
        )
        coEvery { workflow.execute(any()) } returns successResult

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        val effects = mutableListOf<DailyFeedEffect>()
        val job = launch {
            viewModel.uiEffect.toList(effects)
        }

        viewModel.handleEvent(DailyFeedEvent.OnStartCard("c_kotlin_basics"))
        runCurrent()

        assertEquals(1, effects.size)
        assertEquals(DailyFeedEffect.NavigateToCard("c_kotlin_basics"), effects.first())
        job.cancel()
    }

    @Test
    fun testOnCompletePreferences_emitsNavigateToOnboardingEffect() = runTest(testDispatcher) {
        val successResult = DailyLearningInitializationResult.Success(
            userId = UserIdentity.Guest.DEFAULT_GUEST_ID,
            context = mockk(relaxed = true),
            feedDecision = mockk(relaxed = true),
            initializationState = DailyInitializationState.NEW_GUEST_INITIALIZED
        )
        coEvery { workflow.execute(any()) } returns successResult

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        val effects = mutableListOf<DailyFeedEffect>()
        val job = launch {
            viewModel.uiEffect.toList(effects)
        }

        viewModel.handleEvent(DailyFeedEvent.OnCompletePreferences)
        runCurrent()

        assertEquals(1, effects.size)
        assertEquals(DailyFeedEffect.NavigateToOnboarding, effects.first())
        job.cancel()
    }
}
