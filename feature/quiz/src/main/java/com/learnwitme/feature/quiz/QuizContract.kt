package com.learnwitme.feature.quiz

import com.learnwitme.core.domain.model.QuizAttempt
import com.learnwitme.core.domain.model.QuizDecision
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface QuizState : UiState {
    object Idle : QuizState
    object Loading : QuizState
    data class Active(val attempt: QuizAttempt, val currentQuestionIndex: Int) : QuizState
    data class Finished(val decision: QuizDecision) : QuizState
    data class Error(val message: String) : QuizState
}

sealed interface QuizEvent : UiEvent {
    data class OnStartQuiz(val quizId: String) : QuizEvent
    data class OnSubmitAnswer(val questionId: String, val selectedOptionIndex: Int) : QuizEvent
    object OnNextQuestion : QuizEvent
    object OnFinishQuiz : QuizEvent
}

sealed interface QuizEffect : UiEffect {
    data class ShowToast(val message: String) : QuizEffect
    object NavigateBack : QuizEffect
}
