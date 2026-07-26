package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.QuizRepository
import com.learnwitme.core.domain.service.Clock

class StartQuizWorkflow(
    private val quizRepository: QuizRepository,
    private val clock: Clock
) {
    suspend fun execute(userId: UserId, quizId: QuizId): QuizAttempt {
        val quiz = quizRepository.getQuiz(quizId)
            ?: throw IllegalStateException("Quiz ${quizId.value} not found")
        val attemptId = QuizAttemptId("att_${quizId.value}_${clock.currentTimeMillis()}")
        return QuizAttempt(attemptId, userId, quiz)
    }
}
