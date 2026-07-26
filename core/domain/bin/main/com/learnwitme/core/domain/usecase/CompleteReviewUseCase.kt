package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.ReviewCompletedEvent
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.service.SpacedRepetitionEngine

class CompleteReviewUseCase(
    private val learningRepository: LearningRepository,
    private val spacedRepetitionEngine: SpacedRepetitionEngine
) {
    suspend fun execute(
        userId: UserId,
        cardId: KnowledgeCardId,
        qualityScore: Double,
        currentTimeMs: Long
    ): ReviewCompletedEvent {
        val currentSchedule = learningRepository.findReviewSchedule(userId, cardId)
            ?: ReviewSchedule(cardId = cardId, intervalDays = 1, nextReviewTimestamp = currentTimeMs + 86400000L, easeFactor = 2.5, repetitions = 0)

        val updatedSchedule = spacedRepetitionEngine.calculateNextSchedule(currentSchedule, qualityScore, currentTimeMs)
        learningRepository.recordReview(userId, cardId, updatedSchedule)

        return ReviewCompletedEvent(
            eventId = "evt_rev_comp_${userId.value}_${cardId.value}_$currentTimeMs",
            occurredAt = currentTimeMs,
            aggregateId = userId.value,
            cardId = cardId.value,
            qualityScore = qualityScore,
            nextReviewTimestamp = updatedSchedule.nextReviewTimestamp
        )
    }
}
