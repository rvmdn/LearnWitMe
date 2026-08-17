package com.learnwitme.core.application.ai.feedback

/**
 * Immutable model representing learner feedback about AI-generated learning content.
 */
data class AILearningFeedback(
    val interactionId: String,
    val usefulnessRating: Int,
    val confidenceSignal: Double? = null,
    val comment: String? = null,
    val createdAt: Long
) {
    init {
        require(interactionId.isNotBlank()) { "Interaction ID cannot be blank" }
        require(usefulnessRating in 1..5) { "Usefulness rating must be bounded between 1 and 5 (inclusive)" }
        confidenceSignal?.let {
            require(it in 0.0..1.0) { "Confidence signal must be bounded between 0.0 and 1.0" }
        }
        comment?.let {
            require(it.isNotBlank()) { "Comment cannot be blank if provided" }
        }
        require(createdAt > 0) { "CreatedAt timestamp must be positive" }
    }
}
