package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Topic

/**
 * Deterministic classification of the learning-memory effect derived from an AI learning interaction.
 */
enum class AILearningImpactClassification {
    POSITIVE_REINFORCEMENT,
    CONFIDENCE_BOOST,
    CONFIDENCE_REDUCTION,
    REMEDIATION_SIGNAL,
    NEUTRAL_ENGAGEMENT,
    NO_IMPACT
}

/**
 * Immutable model representing the deterministic learning-memory impact derived from an accepted AI interaction/feedback.
 */
data class AILearningMemoryImpact(
    val interactionId: String,
    val cardId: KnowledgeCardId? = null,
    val topic: Topic? = null,
    val action: AIInteractionType,
    val outcomeStatus: AIOutcomeStatus? = null,
    val usefulnessRating: Int,
    val confidenceSignal: Double? = null,
    val classification: AILearningImpactClassification,
    val confidenceDelta: Double = 0.0,
    val calculatedAt: Long
) {
    val topicCode: String? get() = topic?.code

    init {
        require(interactionId.isNotBlank()) { "Interaction ID cannot be blank" }
        require(usefulnessRating in 1..5) { "Usefulness rating must be between 1 and 5" }
        confidenceSignal?.let {
            require(it in 0.0..1.0) { "Confidence signal must be between 0.0 and 1.0" }
        }
        require(calculatedAt > 0) { "CalculatedAt timestamp must be positive" }
    }
}
