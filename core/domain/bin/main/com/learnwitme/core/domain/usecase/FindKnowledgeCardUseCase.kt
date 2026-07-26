package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.service.DifficultyEvaluator

data class KnowledgeCardDetails(
    val card: KnowledgeCard,
    val effectiveDifficulty: DifficultyLevel
)

class FindKnowledgeCardUseCase(
    private val knowledgeRepository: KnowledgeRepository,
    private val learningRepository: LearningRepository,
    private val difficultyEvaluator: DifficultyEvaluator
) {
    suspend fun execute(userId: UserId, cardId: KnowledgeCardId): KnowledgeCardDetails? {
        val card = knowledgeRepository.findCard(cardId) ?: return null
        val userKnowledge = learningRepository.findUserKnowledge(userId)

        val effectiveDifficulty = if (userKnowledge != null) {
            difficultyEvaluator.evaluateEffectiveDifficulty(card, userKnowledge)
        } else {
            card.difficulty
        }

        return KnowledgeCardDetails(card, effectiveDifficulty)
    }
}
