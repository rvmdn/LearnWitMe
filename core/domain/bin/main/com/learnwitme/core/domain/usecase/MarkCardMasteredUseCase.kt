package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.CardMasteredEvent
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository

class MarkCardMasteredUseCase(
    private val learningRepository: LearningRepository
) {
    suspend fun execute(userId: UserId, cardId: KnowledgeCardId, currentTimeMs: Long): CardMasteredEvent {
        val existingKnowledge = learningRepository.findUserKnowledge(userId)
            ?: UserKnowledge(id = userId)

        val updatedKnowledge = existingKnowledge.markCardMastered(cardId)
        learningRepository.saveUserKnowledge(updatedKnowledge)
        learningRepository.markCardMastered(userId, cardId)

        return CardMasteredEvent(
            eventId = "evt_card_mstr_${userId.value}_${cardId.value}_$currentTimeMs",
            occurredAt = currentTimeMs,
            aggregateId = userId.value,
            cardId = cardId.value
        )
    }
}
