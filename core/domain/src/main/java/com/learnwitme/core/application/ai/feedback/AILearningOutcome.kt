package com.learnwitme.core.application.ai.feedback

/**
 * Immutable model representing the outcome of an AI-assisted learning interaction.
 */
data class AILearningOutcome(
    val interactionId: String,
    val status: AIOutcomeStatus,
    val summary: String? = null,
    val completedAt: Long,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(interactionId.isNotBlank()) { "Interaction ID cannot be blank" }
        require(completedAt > 0) { "CompletedAt timestamp must be positive" }
        summary?.let {
            require(it.isNotBlank()) { "Summary cannot be blank if provided" }
        }
    }
}
