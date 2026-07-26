package com.learnwitme.core.application.service

import com.learnwitme.core.application.dispatcher.DispatcherProvider
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.repository.RecommendationRepository
import com.learnwitme.core.domain.usecase.DailyRecommendationResult
import com.learnwitme.core.domain.usecase.GenerateDailyRecommendationUseCase
import kotlinx.coroutines.withContext

/**
 * Responsibility: Application service orchestrating Home/Feed screen workflows, aggregating
 * daily recommendations, recommendation history, and user learning progress metrics.
 */
class HomeApplicationService(
    private val generateDailyRecommendationUseCase: GenerateDailyRecommendationUseCase,
    private val learningRepository: LearningRepository,
    private val recommendationRepository: RecommendationRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun generateDailyFeed(
        userId: UserId,
        availableCards: List<KnowledgeCard>,
        currentTimeMs: Long
    ): AppResult<DailyRecommendationResult> = withContext(dispatcherProvider.io) {
        try {
            val result = generateDailyRecommendationUseCase.execute(userId, availableCards, currentTimeMs)
            AppResult.Success(result)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }

    suspend fun loadRecommendations(userId: UserId): AppResult<Recommendation?> = withContext(dispatcherProvider.io) {
        try {
            val rec = recommendationRepository.findRecommendation(userId)
            AppResult.Success(rec)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }

    suspend fun loadLearningProgress(userId: UserId): AppResult<UserKnowledge?> = withContext(dispatcherProvider.io) {
        try {
            val userKnowledge = learningRepository.findUserKnowledge(userId)
            AppResult.Success(userKnowledge)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }
}
