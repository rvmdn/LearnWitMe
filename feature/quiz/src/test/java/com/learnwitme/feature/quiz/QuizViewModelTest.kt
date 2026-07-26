package com.learnwitme.feature.quiz

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.FinishQuizWorkflow
import com.learnwitme.core.domain.usecase.StartQuizWorkflow
import com.learnwitme.core.domain.usecase.SubmitQuizAnswerWorkflow
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
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val startWorkflow = mockk<StartQuizWorkflow>()
    private val submitWorkflow = mockk<SubmitQuizAnswerWorkflow>()
    private val finishWorkflow = mockk<FinishQuizWorkflow>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartQuizSuccess() = runTest(testDispatcher) {
        val quizId = "qz1"
        val quiz = Quiz(QuizId(quizId), "SQL", listOf(QuizQuestion("q1", "Text", listOf(QuizOption("A", "Exp"), QuizOption("B", "Exp2")), 0)), DifficultyLevel.BEGINNER)
        val attempt = QuizAttempt(QuizAttemptId("att1"), UserId("current_user"), quiz)
        coEvery { startWorkflow.execute(any(), QuizId(quizId)) } coAnswers {
            kotlinx.coroutines.delay(1000)
            attempt
        }

        val viewModel = QuizViewModel(startWorkflow, submitWorkflow, finishWorkflow)
        viewModel.onEvent(QuizEvent.OnStartQuiz(quizId))
        runCurrent()
        assertTrue(viewModel.uiState.value is QuizState.Loading)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is QuizState.Active)
        assertEquals(attempt, (viewModel.uiState.value as QuizState.Active).attempt)
    }
}
