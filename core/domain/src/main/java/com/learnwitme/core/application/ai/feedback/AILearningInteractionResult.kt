package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.model.LearningMemory

/**
 * Immutable structured result of executing an AI-assisted learning interaction,
 * bundling the typed AI output, interaction record, optional outcome, feedback, and Learning Memory projection.
 */
data class AILearningInteractionResult<out T>(
    val generatedResult: T,
    val interaction: AILearningInteraction,
    val outcome: AILearningOutcome? = null,
    val feedback: AILearningFeedback? = null,
    val feedbackResult: AIInteractionFeedbackResult? = null,
    val memoryIntegrationResult: AILearningMemoryIntegrationResult? = null
) {
    /**
     * The projected updated LearningMemory aggregate if feedback was provided and successfully integrated.
     */
    val updatedMemory: LearningMemory?
        get() = (memoryIntegrationResult as? AILearningMemoryIntegrationResult.Success)?.updatedMemory

    /**
     * True if feedback was provided and accepted.
     */
    val isFeedbackAccepted: Boolean
        get() = feedbackResult is AIInteractionFeedbackResult.Accepted

    /**
     * True if feedback was provided but rejected due to validation failures.
     */
    val isFeedbackRejected: Boolean
        get() = feedbackResult is AIInteractionFeedbackResult.Rejected

    /**
     * True if the feedback was successfully integrated into the Learning Memory projection.
     */
    val isMemoryIntegrated: Boolean
        get() = memoryIntegrationResult is AILearningMemoryIntegrationResult.Success

    /**
     * The calculated learning memory impact if integration succeeded.
     */
    val memoryImpact: AILearningMemoryImpact?
        get() = (memoryIntegrationResult as? AILearningMemoryIntegrationResult.Success)?.impact
}
