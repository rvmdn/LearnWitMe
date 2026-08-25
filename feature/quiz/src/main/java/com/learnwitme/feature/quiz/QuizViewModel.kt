package com.learnwitme.feature.quiz

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.FinishQuizWorkflow
import com.learnwitme.core.domain.usecase.StartQuizWorkflow
import com.learnwitme.core.domain.usecase.SubmitQuizAnswerWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val startQuizWorkflow: StartQuizWorkflow,
    private val submitQuizAnswerWorkflow: SubmitQuizAnswerWorkflow,
    private val finishQuizWorkflow: FinishQuizWorkflow
) : BaseViewModel<QuizState, QuizEvent, QuizEffect>() {

    private val currentUserId = UserIdentity.Guest.DEFAULT_GUEST_ID

    override fun createInitialState(): QuizState = QuizState.Idle

    override fun handleEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.OnStartQuiz -> startQuiz(event.quizId)
            is QuizEvent.OnSubmitAnswer -> submitAnswer(event.questionId, event.selectedOptionIndex)
            is QuizEvent.OnNextQuestion -> nextQuestion()
            is QuizEvent.OnFinishQuiz -> finishQuiz()
        }
    }

    private fun startQuiz(quizId: String) {
        viewModelScope.launch {
            setState { QuizState.Loading }
            try {
                val attempt = startQuizWorkflow.execute(currentUserId, QuizId(quizId))
                setState { QuizState.Active(attempt, 0) }
            } catch (e: Exception) {
                setState { QuizState.Error(e.message ?: "Failed to start quiz") }
            }
        }
    }

    private fun submitAnswer(questionId: String, selectedOptionIndex: Int) {
        val currentState = currentState
        if (currentState is QuizState.Active) {
            try {
                val updatedAttempt = submitQuizAnswerWorkflow.execute(currentState.attempt, questionId, selectedOptionIndex)
                setState { currentState.copy(attempt = updatedAttempt) }
            } catch (e: Exception) {
                setEffect { QuizEffect.ShowToast(e.message ?: "Failed to submit answer") }
            }
        }
    }

    private fun nextQuestion() {
        val currentState = currentState
        if (currentState is QuizState.Active) {
            val nextIndex = currentState.currentQuestionIndex + 1
            if (nextIndex < currentState.attempt.quiz.questions.size) {
                setState { currentState.copy(currentQuestionIndex = nextIndex) }
            } else {
                setEffect { QuizEffect.ShowToast("No more questions. Click Finish Quiz!") }
            }
        }
    }

    private fun finishQuiz() {
        val currentState = currentState
        if (currentState is QuizState.Active) {
            viewModelScope.launch {
                setState { QuizState.Loading }
                try {
                    val decision = finishQuizWorkflow.execute(currentUserId, currentState.attempt)
                    setState { QuizState.Finished(decision) }
                } catch (e: Exception) {
                    setState { QuizState.Error(e.message ?: "Failed to finish quiz") }
                }
            }
        }
    }
}
