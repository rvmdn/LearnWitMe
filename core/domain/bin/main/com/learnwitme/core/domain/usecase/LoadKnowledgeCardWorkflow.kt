package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.repository.KnowledgeRepository

class LoadKnowledgeCardWorkflow(
    private val knowledgeRepository: KnowledgeRepository
) {
    suspend fun execute(cardId: KnowledgeCardId): KnowledgeCard? {
        return knowledgeRepository.getCard(cardId)
    }
}
