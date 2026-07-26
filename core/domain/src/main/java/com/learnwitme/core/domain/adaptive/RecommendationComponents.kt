package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Responsibility: Candidate candidate collector component that categorizes candidate cards into candidate pools.
 */
class RecommendationCollector {
    data class CandidatePool(
        val dueCards: List<KnowledgeCard>,
        val weakSkillCards: List<KnowledgeCard>,
        val newTopicCards: List<KnowledgeCard>,
        val challengeCards: List<KnowledgeCard>
    )

    fun collectCandidates(
        availableCards: List<KnowledgeCard>,
        dueCardIds: Set<String>,
        activeCardIds: Set<String>,
        masteredCardIds: Set<String>,
        targetDifficulty: AdaptiveDifficultyLevel
    ): CandidatePool {
        val uniqueCards = availableCards.distinctBy { it.id.value }

        val due = mutableListOf<KnowledgeCard>()
        val weak = mutableListOf<KnowledgeCard>()
        val newTopics = mutableListOf<KnowledgeCard>()
        val challenge = mutableListOf<KnowledgeCard>()

        for (card in uniqueCards) {
            val idStr = card.id.value
            val isDue = dueCardIds.contains(idStr)
            val isStudying = activeCardIds.contains(idStr)
            val isMastered = masteredCardIds.contains(idStr)

            when {
                isDue -> due.add(card)
                isStudying && !isMastered -> weak.add(card)
                !isStudying && !isMastered -> newTopics.add(card)
                else -> challenge.add(card)
            }
        }

        return CandidatePool(
            dueCards = due,
            weakSkillCards = weak,
            newTopicCards = newTopics,
            challengeCards = challenge
        )
    }
}

/**
 * Responsibility: Evaluates candidate pools against difficulty recommendations to assign priority and reason scores.
 */
class RecommendationEvaluator {
    fun evaluateCandidates(
        candidates: RecommendationCollector.CandidatePool,
        difficultyRecommendation: DifficultyRecommendation
    ): List<LearningRecommendation> {
        val recommendations = mutableListOf<LearningRecommendation>()

        // 1. Due Reviews -> CRITICAL priority
        for (card in candidates.dueCards) {
            val reason = if (difficultyRecommendation.reason == DifficultyAdjustmentReason.LONG_INACTIVITY) {
                RecommendationReason.LONG_INACTIVITY
            } else {
                RecommendationReason.REVIEW_DUE
            }
            recommendations.add(
                LearningRecommendation(
                    card = card,
                    reason = reason,
                    priority = RecommendationPriority.CRITICAL,
                    score = 4.0,
                    explanation = "Spaced repetition review due for card ${card.content.title}."
                )
            )
        }

        // 2. Weak Skills -> HIGH priority
        for (card in candidates.weakSkillCards) {
            recommendations.add(
                LearningRecommendation(
                    card = card,
                    reason = RecommendationReason.WEAK_SKILL,
                    priority = RecommendationPriority.HIGH,
                    score = 3.0,
                    explanation = "Reinforce active unmastered topic ${card.content.title}."
                )
            )
        }

        // 3. New Topics -> NORMAL priority
        for (card in candidates.newTopicCards) {
            recommendations.add(
                LearningRecommendation(
                    card = card,
                    reason = RecommendationReason.NEW_TOPIC,
                    priority = RecommendationPriority.NORMAL,
                    score = 2.0,
                    explanation = "Explore new content ${card.content.title} at ${difficultyRecommendation.targetLevel.name} level."
                )
            )
        }

        // 4. Challenge / Mastered Topics -> LOW priority
        for (card in candidates.challengeCards) {
            recommendations.add(
                LearningRecommendation(
                    card = card,
                    reason = RecommendationReason.CHALLENGE,
                    priority = RecommendationPriority.LOW,
                    score = 1.0,
                    explanation = "Challenge/review mastered topic ${card.content.title}."
                )
            )
        }

        return recommendations
    }
}

/**
 * Responsibility: Prioritizes and filters candidate recommendations deterministically according to policy.
 */
class RecommendationPrioritizer {
    fun prioritizeAndFilter(
        recommendations: List<LearningRecommendation>,
        policy: RecommendationPolicy
    ): List<LearningRecommendation> {
        val sorted = recommendations.sortedWith(
            compareBy<LearningRecommendation> { it.priority.ordinal }
                .thenByDescending { it.score }
                .thenBy { it.card.id.value }
        )

        val uniqueByCard = sorted.distinctBy { it.card.id.value }

        val filtered = uniqueByCard.filter { rec ->
            when (policy.minPriorityFilter) {
                RecommendationPriority.CRITICAL -> rec.priority == RecommendationPriority.CRITICAL
                RecommendationPriority.HIGH -> rec.priority == RecommendationPriority.CRITICAL || rec.priority == RecommendationPriority.HIGH
                RecommendationPriority.NORMAL -> rec.priority != RecommendationPriority.LOW
                RecommendationPriority.LOW -> true
            }
        }

        return filtered.take(policy.maxRecommendations)
    }
}
