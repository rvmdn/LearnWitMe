package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.repository.AILearningInteractionRepository

/**
 * Persistence coordinator for AI learning interaction artifacts.
 *
 * Connects the [AILearningInteractionResult] produced by [AILearningInteractionService]
 * to the domain repository contract ([AILearningInteractionRepository]).
 *
 * Persistence Policy:
 * - Interaction entity is ALWAYS persisted.
 * - Outcome entity is persisted if present.
 * - Feedback entity is persisted ONLY if feedback was provided and accepted.
 * - Memory Impact entity is persisted ONLY if feedback was accepted AND memory integration succeeded.
 * - Enforces matching [interactionId] across all non-null artifacts.
 */
class AILearningInteractionPersistenceCoordinator(
    private val repository: AILearningInteractionRepository,
    private val onResultPersisted: (suspend (AILearningInteractionResult<*>) -> Unit)? = null
) {
    /**
     * Persists valid artifacts from an AI interaction result atomically via the repository,
     * and notifies registered persistence observers.
     */
    suspend fun persist(result: AILearningInteractionResult<*>): Boolean {
        val interaction = result.interaction
        val outcome = result.outcome

        val feedbackToPersist = if (result.isFeedbackAccepted) {
            result.feedback
        } else {
            null
        }

        val memoryImpactToPersist = if (result.isFeedbackAccepted && result.isMemoryIntegrated) {
            result.memoryImpact
        } else {
            null
        }

        outcome?.let {
            require(it.interactionId == interaction.interactionId) {
                "Outcome interactionId (${it.interactionId}) does not match interaction interactionId (${interaction.interactionId})"
            }
        }
        feedbackToPersist?.let {
            require(it.interactionId == interaction.interactionId) {
                "Feedback interactionId (${it.interactionId}) does not match interaction interactionId (${interaction.interactionId})"
            }
        }
        memoryImpactToPersist?.let {
            require(it.interactionId == interaction.interactionId) {
                "MemoryImpact interactionId (${it.interactionId}) does not match interaction interactionId (${interaction.interactionId})"
            }
        }

        repository.saveArtifacts(
            interaction = interaction,
            outcome = outcome,
            feedback = feedbackToPersist,
            impact = memoryImpactToPersist
        )

        onResultPersisted?.invoke(result)

        return true
    }
}
