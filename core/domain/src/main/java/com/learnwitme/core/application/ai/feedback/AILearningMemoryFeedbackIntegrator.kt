package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.model.LearningMemory

/**
 * Application-level coordinator that validates AI feedback, computes its deterministic Learning Memory impact,
 * and maps the impact into the existing Learning Memory aggregate projection without performing persistence.
 */
class AILearningMemoryFeedbackIntegrator(
    private val validator: AIInteractionFeedbackValidator = AIInteractionFeedbackValidator(),
    private val calculator: AILearningMemoryImpactCalculator = AILearningMemoryImpactCalculator()
) {

    /**
     * Integrates an accepted AI feedback result with an optional LearningMemory aggregate.
     */
    fun integrateAccepted(
        acceptedResult: AIInteractionFeedbackResult.Accepted,
        currentMemory: LearningMemory? = null
    ): AILearningMemoryIntegrationResult {
        return integrate(
            interaction = acceptedResult.interaction,
            feedback = acceptedResult.feedback,
            outcome = acceptedResult.outcome,
            currentMemory = currentMemory
        )
    }

    /**
     * Validates interaction, feedback, and optional outcome before calculating deterministic Learning Memory impact.
     */
    fun integrate(
        interaction: AILearningInteraction,
        feedback: AILearningFeedback,
        outcome: AILearningOutcome? = null,
        currentMemory: LearningMemory? = null
    ): AILearningMemoryIntegrationResult {
        val rejection = validator.validate(interaction, feedback, outcome)
        if (rejection != null) {
            return AILearningMemoryIntegrationResult.Rejected(
                interactionId = interaction.interactionId,
                reason = AILearningMemoryIntegrationRejectionReason.FEEDBACK_VALIDATION_FAILED,
                message = rejection.message
            )
        }

        if (currentMemory != null && currentMemory.userId != interaction.userId) {
            return AILearningMemoryIntegrationResult.Rejected(
                interactionId = interaction.interactionId,
                reason = AILearningMemoryIntegrationRejectionReason.USER_ID_MISMATCH,
                message = "Current LearningMemory userId (${currentMemory.userId.value}) does not match interaction userId (${interaction.userId.value})"
            )
        }

        val impact = calculator.calculateImpact(interaction, feedback, outcome)

        val updatedMemory = currentMemory?.let { memory ->
            val cardId = impact.cardId
            if (cardId != null) {
                when {
                    impact.confidenceDelta > 0.0 -> memory.increaseConfidence(cardId, impact.confidenceDelta)
                    impact.confidenceDelta < 0.0 -> memory.decreaseConfidence(cardId, kotlin.math.abs(impact.confidenceDelta))
                    else -> memory
                }
            } else {
                memory
            }
        }

        return AILearningMemoryIntegrationResult.Success(
            interactionId = interaction.interactionId,
            impact = impact,
            updatedMemory = updatedMemory
        )
    }
}
