package com.learnwitme.core.analytics

/**
 * Immutable analytics events capturing facts about AI-assisted learning interactions,
 * outcomes, feedback, and memory impact projections.
 *
 * Data Privacy & Sensitivity Policy:
 * - Includes correlation IDs ([interactionId], [userId]).
 * - Includes domain metadata ([action], [topicCode], [cardId], [provider], [model]).
 * - EXCLUDES raw AI prompts, system prompts, free-text comments, and security credentials.
 */
sealed interface AILearningAnalyticsEvent {
    val eventId: String
    val timestamp: Long
    val interactionId: String
    val userId: String
    val action: String

    /**
     * Emitted when an AI-assisted learning interaction completes execution.
     */
    data class InteractionCompleted(
        override val eventId: String,
        override val timestamp: Long,
        override val interactionId: String,
        override val userId: String,
        override val action: String,
        val cardId: String?,
        val topicCode: String?,
        val provider: String?,
        val model: String?,
        val promptTokens: Int,
        val completionTokens: Int,
        val totalTokens: Int,
        val personalizationPreset: String?
    ) : AILearningAnalyticsEvent

    /**
     * Emitted when an outcome status is recorded for an AI interaction.
     */
    data class OutcomeRecorded(
        override val eventId: String,
        override val timestamp: Long,
        override val interactionId: String,
        override val userId: String,
        override val action: String,
        val outcomeStatus: String,
        val summary: String?
    ) : AILearningAnalyticsEvent

    /**
     * Emitted when valid learner feedback is recorded for an AI interaction.
     */
    data class FeedbackRecorded(
        override val eventId: String,
        override val timestamp: Long,
        override val interactionId: String,
        override val userId: String,
        override val action: String,
        val usefulnessRating: Int,
        val confidenceSignal: Double?,
        val hasComment: Boolean
    ) : AILearningAnalyticsEvent

    /**
     * Emitted when an AI interaction successfully projects an updated Learning Memory impact.
     */
    data class MemoryImpactRecorded(
        override val eventId: String,
        override val timestamp: Long,
        override val interactionId: String,
        override val userId: String,
        override val action: String,
        val classification: String,
        val confidenceDelta: Double,
        val cardId: String?,
        val topicCode: String?
    ) : AILearningAnalyticsEvent
}
