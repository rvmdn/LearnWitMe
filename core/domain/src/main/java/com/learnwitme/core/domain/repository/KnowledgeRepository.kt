package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId

/**
 * Responsibility: Repository contract for managing KnowledgeCard entities.
 */
interface KnowledgeRepository {
    suspend fun getCard(id: KnowledgeCardId): KnowledgeCard?
    suspend fun saveCard(card: KnowledgeCard)
    suspend fun deleteCard(id: KnowledgeCardId)
    suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard>
}
