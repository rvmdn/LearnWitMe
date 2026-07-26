package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.LearningStartedEvent
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository

class StartLearningUseCase(
    private val learningRepository: LearningRepository
) {
    suspend fun execute(userId: UserId, cardId: KnowledgeCardId, currentTimeMs: Long): LearningStartedEvent {
        val existingKnowledge = learningRepository.findUserKnowledge(userId)
            ?: UserKnowledge(id = userId)

        val updatedKnowledge = existingKnowledge.startLearning(cardId)
        learningRepository.saveUserKnowledge(updatedKnowledge)
        learningRepository.startLearning(userId, cardId)

        return LearningStartedEvent(
            eventId = "evt_learn_start_${userId.value}_${cardId.value}_$currentTimeMs",
            occurredAt = currentTimeMs,
            aggregateId = userId.value,
            cardId = cardId.value
        )
    }
}
