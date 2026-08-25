package com.learnwitme.core.domain.repository

import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningInteraction
import com.learnwitme.core.application.ai.feedback.AILearningMemoryImpact
import com.learnwitme.core.application.ai.feedback.AILearningOutcome

/**
 * Persistence contract for the immutable artifacts produced by an AI-assisted learning interaction.
 *
 * This contract persists interaction evidence only. It does not write or update LearningMemory;
 * LearningMemory remains owned by its existing repository and domain invariants.
 */
interface AILearningInteractionRepository {
    suspend fun saveInteraction(interaction: AILearningInteraction)
    suspend fun getInteraction(interactionId: String): AILearningInteraction?

    suspend fun saveFeedback(feedback: AILearningFeedback)
    suspend fun getFeedback(interactionId: String): AILearningFeedback?

    suspend fun saveOutcome(outcome: AILearningOutcome)
    suspend fun getOutcome(interactionId: String): AILearningOutcome?

    suspend fun saveMemoryImpact(impact: AILearningMemoryImpact)
    suspend fun getMemoryImpact(interactionId: String): AILearningMemoryImpact?

    /**
     * Persists interaction artifacts together in a single operation.
     */
    suspend fun saveArtifacts(
        interaction: AILearningInteraction,
        outcome: AILearningOutcome? = null,
        feedback: AILearningFeedback? = null,
        impact: AILearningMemoryImpact? = null
    ) {
        saveInteraction(interaction)
        outcome?.let { saveOutcome(it) }
        feedback?.let { saveFeedback(it) }
        impact?.let { saveMemoryImpact(it) }
    }
}
