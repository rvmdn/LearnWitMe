package com.learnwitme.core.application.ai.feedback

/**
 * Deterministic application-level feedback recorder for AI-assisted learning interactions.
 */
class AILearningFeedbackRecorder(
    private val validator: AIInteractionFeedbackValidator = AIInteractionFeedbackValidator()
) {

    /**
     * Records feedback for an existing AILearningInteraction using an existing AILearningFeedback instance.
     */
    fun recordFeedback(
        interaction: AILearningInteraction,
        feedback: AILearningFeedback,
        outcome: AILearningOutcome? = null
    ): AIInteractionFeedbackResult {
        val rejection = validator.validate(interaction, feedback, outcome)
        if (rejection != null) {
            return rejection
        }

        return AIInteractionFeedbackResult.Accepted(
            interaction = interaction,
            feedback = feedback,
            outcome = outcome
        )
    }

    /**
     * Overload to record feedback by parameters, converting validation failures to structured rejection results.
     */
    fun recordFeedback(
        interaction: AILearningInteraction,
        usefulnessRating: Int,
        confidenceSignal: Double? = null,
        comment: String? = null,
        createdAt: Long,
        outcome: AILearningOutcome? = null
    ): AIInteractionFeedbackResult {
        if (usefulnessRating !in 1..5) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING,
                message = "Usefulness rating ($usefulnessRating) must be bounded between 1 and 5",
                interactionId = interaction.interactionId
            )
        }

        if (confidenceSignal != null && confidenceSignal !in 0.0..1.0) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_CONFIDENCE_SIGNAL,
                message = "Confidence signal ($confidenceSignal) must be bounded between 0.0 and 1.0",
                interactionId = interaction.interactionId
            )
        }

        if (createdAt <= 0) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING,
                message = "CreatedAt timestamp ($createdAt) must be positive",
                interactionId = interaction.interactionId
            )
        }

        if (comment != null && comment.isBlank()) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_FEEDBACK_COMMENT,
                message = "Comment cannot be blank if provided",
                interactionId = interaction.interactionId
            )
        }

        val feedback = try {
            AILearningFeedback(
                interactionId = interaction.interactionId,
                usefulnessRating = usefulnessRating,
                confidenceSignal = confidenceSignal,
                comment = comment,
                createdAt = createdAt
            )
        } catch (e: IllegalArgumentException) {
            return AIInteractionFeedbackResult.Rejected(
                reason = AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING,
                message = e.message ?: "Invalid feedback parameters",
                interactionId = interaction.interactionId
            )
        }

        return recordFeedback(interaction, feedback, outcome)
    }
}
