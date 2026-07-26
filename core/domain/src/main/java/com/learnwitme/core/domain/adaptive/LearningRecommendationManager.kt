package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository

/**
 * Responsibility: High-level recommendation orchestrator coordinating candidate collection,
 * evaluation, prioritization, and adaptive difficulty integration.
 */
class LearningRecommendationManager(
    private val learningRepository: LearningRepository,
    private val difficultyManager: AdaptiveDifficultyManager,
    private val collector: RecommendationCollector = RecommendationCollector(),
    private val evaluator: RecommendationEvaluator = RecommendationEvaluator(),
    private val prioritizer: RecommendationPrioritizer = RecommendationPrioritizer()
) {
    suspend fun generateRecommendations(
        userId: UserId,
        availableCards: List<KnowledgeCard>,
        currentTimeMs: Long,
        policy: RecommendationPolicy = RecommendationPolicy()
    ): List<LearningRecommendation> {
        val userKnowledge = learningRepository.findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val dueReviews = learningRepository.findDueReviews(userId, currentTimeMs)
        val dueCardIdSet = dueReviews.map { it.cardId.value }.toSet()

        val activeCardIds = userKnowledge.activeCardIds.map { it.value }.toSet()
        val masteredCardIds = userKnowledge.masteredCardIds.map { it.value }.toSet()

        val difficultyRec = difficultyManager.evaluateUserDifficulty(userId, currentTimeMs)

        val candidates = collector.collectCandidates(
            availableCards = availableCards,
            dueCardIds = dueCardIdSet,
            activeCardIds = activeCardIds,
            masteredCardIds = masteredCardIds,
            targetDifficulty = difficultyRec.targetLevel
        )

        val rawRecommendations = evaluator.evaluateCandidates(candidates, difficultyRec)
        return prioritizer.prioritizeAndFilter(rawRecommendations, policy)
    }
}
