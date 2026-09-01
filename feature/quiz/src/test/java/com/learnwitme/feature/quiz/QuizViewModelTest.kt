package com.learnwitme.feature.quiz

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.FinishQuizWorkflow
import com.learnwitme.core.domain.usecase.StartQuizWorkflow
import com.learnwitme.core.domain.usecase.SubmitQuizAnswerWorkflow
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
        val quiz = Quiz(
            QuizId(quizId),
            "SQL",
            listOf(QuizQuestion("q1", "Text", listOf(QuizOption("A", "Exp"), QuizOption("B", "Exp2")), 0)),
            DifficultyLevel.BEGINNER
        )
        val attempt = QuizAttempt(QuizAttemptId("att1"), UserIdentity.Guest.DEFAULT_GUEST_ID, quiz)
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

    @Test
    fun testFinishQuizSuccess() = runTest(testDispatcher) {
        val quizId = "qz1"
        val quiz = Quiz(
            QuizId(quizId),
            "SQL",
            listOf(QuizQuestion("q1", "Text", listOf(QuizOption("A", "Exp"), QuizOption("B", "Exp2")), 0)),
            DifficultyLevel.BEGINNER
        )
        val attempt = QuizAttempt(QuizAttemptId("att1"), UserIdentity.Guest.DEFAULT_GUEST_ID, quiz)
        val quizDecision = mockk<QuizDecision>(relaxed = true)
        coEvery { startWorkflow.execute(any(), QuizId(quizId)) } returns attempt
        coEvery { finishWorkflow.execute(any(), any(), any(), any(), any()) } returns quizDecision

        val viewModel = QuizViewModel(startWorkflow, submitWorkflow, finishWorkflow)
        viewModel.onEvent(QuizEvent.OnStartQuiz(quizId))
        advanceUntilIdle()

        viewModel.onEvent(QuizEvent.OnFinishQuiz)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is QuizState.Finished)
        assertEquals(quizDecision, (viewModel.uiState.value as QuizState.Finished).decision)
    }

    @Test
    fun testSubmitAnswer_updatesAttempt() = runTest(testDispatcher) {
        val quizId = "qz1"
        val quiz = Quiz(
            QuizId(quizId),
            "Kotlin",
            listOf(QuizQuestion("q1", "Text", listOf(QuizOption("A", "Exp"), QuizOption("B", "Exp2")), 0)),
            DifficultyLevel.BEGINNER
        )
        val attempt = QuizAttempt(QuizAttemptId("att1"), UserIdentity.Guest.DEFAULT_GUEST_ID, quiz)
        val updatedAttempt = attempt.submitAnswer("q1", 0)

        coEvery { startWorkflow.execute(any(), QuizId(quizId)) } returns attempt
        coEvery { submitWorkflow.execute(attempt, "q1", 0) } returns updatedAttempt

        val viewModel = QuizViewModel(startWorkflow, submitWorkflow, finishWorkflow)
        viewModel.onEvent(QuizEvent.OnStartQuiz(quizId))
        advanceUntilIdle()

        viewModel.onEvent(QuizEvent.OnSubmitAnswer("q1", 0))
        advanceUntilIdle()

        val state = viewModel.uiState.value as QuizState.Active
        assertEquals(updatedAttempt, state.attempt)
    }

    @Test
    fun testStartQuiz_failure_showsErrorState() = runTest(testDispatcher) {
        coEvery { startWorkflow.execute(any(), any()) } throws RuntimeException("Quiz not found")

        val viewModel = QuizViewModel(startWorkflow, submitWorkflow, finishWorkflow)
        viewModel.onEvent(QuizEvent.OnStartQuiz("invalid_quiz"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is QuizState.Error)
        assertEquals("Quiz not found", (viewModel.uiState.value as QuizState.Error).message)
    }
}
