package com.learnwitme.core.application.service

import com.learnwitme.core.application.dispatcher.DispatcherProvider
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.domain.event.ReviewCompletedEvent
import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.usecase.CompleteReviewUseCase
import com.learnwitme.core.domain.usecase.StartLearningUseCase
import kotlinx.coroutines.withContext

/**
 * Responsibility: Application service orchestrating learning interaction workflows
 * (starting card learning, recording review outcomes, and updating skill competencies).
 */
class LearningApplicationService(
    private val startLearningUseCase: StartLearningUseCase,
    private val completeReviewUseCase: CompleteReviewUseCase,
    private val learningRepository: LearningRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun startLearning(userId: UserId, cardId: KnowledgeCardId, currentTimeMs: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            try {
                startLearningUseCase.execute(userId, cardId, currentTimeMs)
                AppResult.Success(Unit)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }

    suspend fun completeReview(
        userId: UserId,
        cardId: KnowledgeCardId,
        qualityScore: Double,
        currentTimeMs: Long
    ): AppResult<ReviewCompletedEvent> = withContext(dispatcherProvider.io) {
        try {
            val event = completeReviewUseCase.execute(userId, cardId, qualityScore, currentTimeMs)
            AppResult.Success(event)
        } catch (e: Throwable) {
            AppResult.Error(e)
        }
    }

    suspend fun updateCompetency(userId: UserId, skill: Skill, competency: Competency): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            try {
                learningRepository.updateCompetency(userId, skill, competency)
                AppResult.Success(Unit)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }
}
