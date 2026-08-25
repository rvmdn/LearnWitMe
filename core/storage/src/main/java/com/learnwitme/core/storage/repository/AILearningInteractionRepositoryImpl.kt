package com.learnwitme.core.storage.repository

import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningInteraction
import com.learnwitme.core.application.ai.feedback.AILearningMemoryImpact
import com.learnwitme.core.application.ai.feedback.AILearningOutcome
import com.learnwitme.core.domain.repository.AILearningInteractionRepository
import com.learnwitme.core.storage.local.dao.AILearningInteractionDao
import com.learnwitme.core.storage.mapper.AILearningInteractionMapper
import javax.inject.Inject

class AILearningInteractionRepositoryImpl @Inject constructor(
    private val dao: AILearningInteractionDao
) : AILearningInteractionRepository {
    override suspend fun saveInteraction(interaction: AILearningInteraction) {
        dao.insertInteraction(AILearningInteractionMapper.toEntity(interaction))
    }

    override suspend fun getInteraction(interactionId: String): AILearningInteraction? =
        dao.getInteraction(interactionId)?.let(AILearningInteractionMapper::toDomain)

    override suspend fun saveFeedback(feedback: AILearningFeedback) {
        dao.insertFeedback(AILearningInteractionMapper.toEntity(feedback))
    }

    override suspend fun getFeedback(interactionId: String): AILearningFeedback? =
        dao.getFeedback(interactionId)?.let(AILearningInteractionMapper::toDomain)

    override suspend fun saveOutcome(outcome: AILearningOutcome) {
        dao.insertOutcome(AILearningInteractionMapper.toEntity(outcome))
    }

    override suspend fun getOutcome(interactionId: String): AILearningOutcome? =
        dao.getOutcome(interactionId)?.let(AILearningInteractionMapper::toDomain)

    override suspend fun saveMemoryImpact(impact: AILearningMemoryImpact) {
        dao.insertMemoryImpact(AILearningInteractionMapper.toEntity(impact))
    }

    override suspend fun getMemoryImpact(interactionId: String): AILearningMemoryImpact? =
        dao.getMemoryImpact(interactionId)?.let(AILearningInteractionMapper::toDomain)

    override suspend fun saveArtifacts(
        interaction: AILearningInteraction,
        outcome: AILearningOutcome?,
        feedback: AILearningFeedback?,
        impact: AILearningMemoryImpact?
    ) {
        dao.insertArtifacts(
            interaction = AILearningInteractionMapper.toEntity(interaction),
            outcome = outcome?.let(AILearningInteractionMapper::toEntity),
            feedback = feedback?.let(AILearningInteractionMapper::toEntity),
            impact = impact?.let(AILearningInteractionMapper::toEntity)
        )
    }
}
