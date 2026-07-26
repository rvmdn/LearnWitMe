package com.learnwitme.core.domain.model

data class RecommendationCandidate(
    val unit: LearningUnit,
    val score: Double,
    val reasons: List<String>
) : ValueObject {
    init {
        require(score >= 0.0) { "Recommendation score cannot be negative" }
        require(reasons.isNotEmpty()) { "Must have at least one reason for recommendation" }
    }
}

data class RecommendationDecision(
    val topRecommendation: RecommendationCandidate?,
    val alternatives: List<RecommendationCandidate>
) : ValueObject

data class RecommendationPolicy(
    val includeMasteredIfOverdue: Boolean = true,
    val maxAlternatives: Int = 3
) : ValueObject
