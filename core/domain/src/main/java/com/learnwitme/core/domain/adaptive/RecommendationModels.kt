package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Business rationale for adaptive content recommendations.
 */
enum class RecommendationReason {
    REVIEW_DUE,
    WEAK_SKILL,
    NEW_TOPIC,
    CHALLENGE,
    CONSISTENT_PROGRESS,
    LONG_INACTIVITY
}

/**
 * Priority levels for adaptive recommendations.
 */
enum class RecommendationPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

/**
 * Individual recommendation item generated for the learner.
 */
data class LearningRecommendation(
    val card: KnowledgeCard,
    val reason: RecommendationReason,
    val priority: RecommendationPriority,
    val score: Double,
    val explanation: String
)

/**
 * Configurable parameters governing recommendation limits and priority filters.
 */
data class RecommendationPolicy(
    val maxRecommendations: Int = 10,
    val includeChallengeTopics: Boolean = true,
    val minPriorityFilter: RecommendationPriority = RecommendationPriority.LOW
)
