package com.learnwitme.core.application.ai.feedback

/**
 * Validates identity, ratings, confidence signals, and timestamp ordering for AI learning feedback.
 */
class AIInteractionFeedbackValidator {

    /**
     * Validates interaction, feedback, and optional outcome consistency.
     * Returns null if valid, or a structured Rejected result if validation fails.
     */
    fun validate(
        interaction: AILearningInteraction,
        feedback: AILearningFeedback,
        outcome: AILearningOutcome? = null
    ): AIInteractionFeedbackResult.Rejected? {
        if (interaction.interactionId.isBlank() || feedback.interactionId.isBlank()) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INTERACTION_ID_MISMATCH,
                message = "Interaction ID cannot be blank",
                interactionId = interaction.interactionId
            )
        }

        if (interaction.interactionId != feedback.interactionId) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INTERACTION_ID_MISMATCH,
                message = "Feedback interaction ID '${feedback.interactionId}' does not match interaction ID '${interaction.interactionId}'",
                interactionId = interaction.interactionId
            )
        }

        if (outcome != null && outcome.interactionId != interaction.interactionId) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INTERACTION_ID_MISMATCH,
                message = "Outcome interaction ID '${outcome.interactionId}' does not match interaction ID '${interaction.interactionId}'",
                interactionId = interaction.interactionId
            )
        }

        if (feedback.usefulnessRating !in 1..5) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING,
                message = "Usefulness rating (${feedback.usefulnessRating}) must be bounded between 1 and 5",
                interactionId = interaction.interactionId
            )
        }

        feedback.confidenceSignal?.let { signal ->
            if (signal !in 0.0..1.0) {
                return AIInteractionFeedbackResult.Rejected(
                    reason = AIInteractionFeedbackRejectionReason.INVALID_CONFIDENCE_SIGNAL,
                    message = "Confidence signal ($signal) must be bounded between 0.0 and 1.0",
                    interactionId = interaction.interactionId
                )
            }
        }

        if (feedback.createdAt < interaction.startedAt) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING,
                message = "Feedback createdAt (${feedback.createdAt}) cannot be earlier than interaction startedAt (${interaction.startedAt})",
                interactionId = interaction.interactionId
            )
        }

        interaction.completedAt?.let { completedAt ->
            if (feedback.createdAt < completedAt) {
                return AIInteractionFeedbackResult.Rejected(
                    reason = AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING,
                    message = "Feedback createdAt (${feedback.createdAt}) cannot be earlier than interaction completedAt ($completedAt)",
                    interactionId = interaction.interactionId
                )
            }
        }

        if (outcome != null && feedback.createdAt < outcome.completedAt) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING,
                message = "Feedback createdAt (${feedback.createdAt}) cannot be earlier than outcome completedAt (${outcome.completedAt})",
                interactionId = interaction.interactionId
            )
        }

        return null
    }
}
