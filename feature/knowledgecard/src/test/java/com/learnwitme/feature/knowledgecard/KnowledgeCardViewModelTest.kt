package com.learnwitme.feature.knowledgecard

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.FinishReviewWorkflow
import com.learnwitme.core.domain.usecase.LoadKnowledgeCardWorkflow
import io.mockk.coEvery
import io.mockk.coVerify
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadCardSuccess() = runTest(testDispatcher) {
        val cardId = KnowledgeCardId("c1")
        val card = KnowledgeCard(
            cardId, "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best", 
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact", 
            emptyList(), emptyList(), listOf("tp_sql")
        )
        coEvery { loadWorkflow.execute(cardId) } returns card

        val viewModel = KnowledgeCardViewModel(loadWorkflow, finishReviewWorkflow)
        viewModel.onEvent(KnowledgeCardEvent.OnLoadCard("c1"))
        
        assertTrue(viewModel.uiState.value is KnowledgeCardState.Loading)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is KnowledgeCardState.Success)
        assertEquals(card, (viewModel.uiState.value as KnowledgeCardState.Success).card)
    }

    @Test
    fun testSubmitReview() = runTest(testDispatcher) {
        val cardId = "c1"
        val viewModel = KnowledgeCardViewModel(loadWorkflow, finishReviewWorkflow)
        
        viewModel.onEvent(KnowledgeCardEvent.OnSubmitReview(cardId, 0.8))
        advanceUntilIdle()
        
        coVerify { finishReviewWorkflow.execute(any(), KnowledgeCardId(cardId), any(), any()) }
    }
}
