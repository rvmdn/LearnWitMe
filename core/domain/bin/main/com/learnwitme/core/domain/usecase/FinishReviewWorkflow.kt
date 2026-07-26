package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

class FinishReviewWorkflow(
    private val memoryRepository: LearningMemoryRepository,
    private val memoryEngine: LearningMemoryEngine,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        cardId: KnowledgeCardId,
        quality: ReviewQuality,
        cardToTopicMapper: (KnowledgeCardId) -> String
    ) {
        val memory = memoryRepository.getMemoryByUserId(userId)
            ?: throw IllegalStateException("Memory not found for user \${userId.value}")
            
        val decision = memoryEngine.processReview(memory, cardId, quality, clock, cardToTopicMapper)
        
        memoryRepository.saveMemory(decision.updatedMemory)
    }
}
