package com.learnwitme.core.storage.mapper

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.storage.local.entity.RecommendationEntity

/**
 * Responsibility: Pure, deterministic bidirectional mapping object converting between Recommendation
 * domain models and Room persistence entities.
 */
object RecommendationMapper {

    fun toEntity(userId: UserId, recommendation: Recommendation): RecommendationEntity {
        return RecommendationEntity(
            userId = userId.value,
            cardId = recommendation.cardId.value,
            score = recommendation.score,
            reason = recommendation.reason
        )
    }

    fun toDomain(entity: RecommendationEntity): Recommendation {
        return Recommendation(
            cardId = KnowledgeCardId(entity.cardId),
            score = entity.score,
            reason = entity.reason
        )
    }
}
