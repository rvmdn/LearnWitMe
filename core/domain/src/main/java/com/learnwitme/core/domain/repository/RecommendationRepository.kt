package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing personalized feed recommendation persistence.
 */
interface RecommendationRepository {
    suspend fun findRecommendation(userId: UserId): Recommendation?
    suspend fun saveRecommendation(userId: UserId, recommendation: Recommendation)
}
