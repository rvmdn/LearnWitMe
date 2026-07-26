package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.Quiz
import com.learnwitme.core.domain.model.QuizId
import com.learnwitme.core.domain.model.QuizResult
import com.learnwitme.core.domain.model.QuizResultId
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing Quiz and QuizResult entities.
 */
interface QuizRepository {
    suspend fun getQuiz(id: QuizId): Quiz?
    suspend fun saveQuiz(quiz: Quiz)
    suspend fun getQuizResult(id: QuizResultId): QuizResult?
    suspend fun saveQuizResult(result: QuizResult)
    suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult>
}
