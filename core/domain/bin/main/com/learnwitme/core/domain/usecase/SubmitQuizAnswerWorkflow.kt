package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.QuizAttempt

class SubmitQuizAnswerWorkflow {
    fun execute(attempt: QuizAttempt, questionId: String, selectedOptionIndex: Int): QuizAttempt {
        return attempt.submitAnswer(questionId, selectedOptionIndex)
    }
}
