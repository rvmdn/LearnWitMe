package com.learnwitme.core.application.ai.feedback

/**
 * Structured rejection reasons for AI learning feedback recording.
 */
enum class AIInteractionFeedbackRejectionReason {
    INTERACTION_ID_MISMATCH,
    INVALID_USEFULNESS_RATING,
    INVALID_CONFIDENCE_SIGNAL,
    INVALID_TIMESTAMP_ORDERING,
    INVALID_FEEDBACK_COMMENT,
    OUTCOME_STATE_INVALID
}

/**
 * Deterministic sealed result representing the outcome of recording AI learning feedback.
 */
sealed interface AIInteractionFeedbackResult {
    data class Accepted(
        val interaction: AILearningInteraction,
        val feedback: AILearningFeedback,
        val outcome: AILearningOutcome? = null
    ) : AIInteractionFeedbackResult

    data class Rejected(
        val reason: AIInteractionFeedbackRejectionReason,
        val message: String,
        val interactionId: String? = null
    ) : AIInteractionFeedbackResult

    val isAccepted: Boolean get() = this is Accepted
    val isRejected: Boolean get() = this is Rejected
}
