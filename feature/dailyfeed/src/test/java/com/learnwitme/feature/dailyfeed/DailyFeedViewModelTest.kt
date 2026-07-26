package com.learnwitme.feature.dailyfeed

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.BuildDailyFeedWorkflow
import io.mockk.coEvery
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
class DailyFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val workflow = mockk<BuildDailyFeedWorkflow>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadSuccess() = runTest(testDispatcher) {
        val decision = FeedDecision(
            feed = DailyFeed(emptyList()),
            layout = FeedLayout("default"),
            summary = FeedSummary(0)
        )
        coEvery { workflow.execute(any()) } returns decision

        val viewModel = DailyFeedViewModel(workflow)
        
        // Initial state is loading
        assertTrue(viewModel.uiState.value is DailyFeedState.Loading)
        
        // Advance coroutines
        advanceUntilIdle()
        
        // State should be success
        assertTrue(viewModel.uiState.value is DailyFeedState.Success)
        assertEquals(decision, (viewModel.uiState.value as DailyFeedState.Success).feed)
    }

    @Test
    fun testRefreshEvent() = runTest(testDispatcher) {
        val decision = FeedDecision(
            feed = DailyFeed(emptyList()),
            layout = FeedLayout("default"),
            summary = FeedSummary(0)
        )
        coEvery { workflow.execute(any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            decision
        }

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()
        
        viewModel.onEvent(DailyFeedEvent.OnRefresh)
        runCurrent()
        assertTrue(viewModel.uiState.value is DailyFeedState.Loading)
        
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DailyFeedState.Success)
    }

    @Test
    fun testErrorHandling() = runTest(testDispatcher) {
        coEvery { workflow.execute(any()) } throws IllegalStateException("No user profile")

        val viewModel = DailyFeedViewModel(workflow)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is DailyFeedState.Error)
        assertEquals("No user profile", (viewModel.uiState.value as DailyFeedState.Error).message)
    }
}
