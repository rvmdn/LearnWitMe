package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.DailyFeedGeneratedEvent
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.repository.RecommendationRepository
import com.learnwitme.core.domain.service.RecommendationEngine

data class DailyRecommendationResult(
    val recommendations: List<Recommendation>,
    val event: DailyFeedGeneratedEvent
)

class GenerateDailyRecommendationUseCase(
    private val knowledgeRepository: KnowledgeRepository,
    private val learningRepository: LearningRepository,
    private val recommendationRepository: RecommendationRepository,
    private val recommendationEngine: RecommendationEngine
) {
    suspend fun execute(
        userId: UserId,
        availableCards: List<KnowledgeCard>,
        currentTimeMs: Long
    ): DailyRecommendationResult {
        val userKnowledge = learningRepository.findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val dueSchedules = learningRepository.findDueReviews(userId, currentTimeMs)

        val recommendations = recommendationEngine.generateKnowledgeRecommendation(
            userId = userId,
            userKnowledge = userKnowledge,
            dueSchedules = dueSchedules,
            availableCards = availableCards,
            currentTimeMs = currentTimeMs
        )

        val topRecommendation = recommendations.firstOrNull()
        if (topRecommendation != null) {
            recommendationRepository.saveRecommendation(userId, topRecommendation)
        }

        val event = DailyFeedGeneratedEvent(
            eventId = "evt_feed_gen_${userId.value}_$currentTimeMs",
            occurredAt = currentTimeMs,
            aggregateId = userId.value,
            itemCount = recommendations.size,
            topFocusCardId = topRecommendation?.cardId?.value
        )

        return DailyRecommendationResult(recommendations, event)
    }
}
