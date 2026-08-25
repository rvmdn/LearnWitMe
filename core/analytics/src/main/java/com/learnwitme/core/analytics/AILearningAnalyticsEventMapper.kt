package com.learnwitme.core.analytics

import com.learnwitme.core.application.ai.feedback.AILearningInteractionResult

/**
 * Deterministic mapper converting application-level AI interaction results
 * ([AILearningInteractionResult]) into structured analytics events ([AILearningAnalyticsEvent]).
 *
 * Principles:
 * - Pure mapping function with zero side effects.
 * - Enforces privacy rules by excluding raw free-text comments and secrets.
 * - Produces events strictly for valid artifacts present in the result.
 */
class AILearningAnalyticsEventMapper {

    fun mapToAnalyticsEvents(result: AILearningInteractionResult<*>): List<AILearningAnalyticsEvent> {
        val events = mutableListOf<AILearningAnalyticsEvent>()
        val interaction = result.interaction
        val timestamp = interaction.completedAt ?: interaction.startedAt

        // 1. Interaction Completed Event (Always generated)
        events.add(
            AILearningAnalyticsEvent.InteractionCompleted(
                eventId = "evt_interaction_${interaction.interactionId}",
                timestamp = timestamp,
                interactionId = interaction.interactionId,
                userId = interaction.userId.value,
                action = interaction.action.name,
                cardId = interaction.cardId?.value,
                topicCode = interaction.topic?.code,
                provider = interaction.provider?.name,
                model = interaction.model,
                promptTokens = interaction.promptTokens,
                completionTokens = interaction.completionTokens,
                totalTokens = interaction.totalTokens,
                personalizationPreset = interaction.personalizationPreset
            )
        )

        // 2. Outcome Event (Generated if outcome exists)
        result.outcome?.let { outcome ->
            events.add(
                AILearningAnalyticsEvent.OutcomeRecorded(
                    eventId = "evt_outcome_${interaction.interactionId}",
                    timestamp = outcome.completedAt,
                    interactionId = interaction.interactionId,
                    userId = interaction.userId.value,
                    action = interaction.action.name,
                    outcomeStatus = outcome.status.name,
                    summary = outcome.summary
                )
            )
        }

        // 3. Feedback Event (Generated ONLY if feedback is provided AND accepted)
        if (result.isFeedbackAccepted) {
            result.feedback?.let { feedback ->
                events.add(
                    AILearningAnalyticsEvent.FeedbackRecorded(
                        eventId = "evt_feedback_${interaction.interactionId}",
                        timestamp = feedback.createdAt,
                        interactionId = interaction.interactionId,
                        userId = interaction.userId.value,
                        action = interaction.action.name,
                        usefulnessRating = feedback.usefulnessRating,
                        confidenceSignal = feedback.confidenceSignal,
                        hasComment = !feedback.comment.isNullOrBlank()
                    )
                )
            }
        }

        // 4. Memory Impact Event (Generated ONLY if feedback accepted AND memory integration succeeded)
        if (result.isMemoryIntegrated) {
            result.memoryImpact?.let { impact ->
                events.add(
                    AILearningAnalyticsEvent.MemoryImpactRecorded(
                        eventId = "evt_impact_${interaction.interactionId}",
                        timestamp = impact.calculatedAt,
                        interactionId = interaction.interactionId,
                        userId = interaction.userId.value,
                        action = interaction.action.name,
                        classification = impact.classification.name,
                        confidenceDelta = impact.confidenceDelta,
                        cardId = impact.cardId?.value,
                        topicCode = impact.topic?.code
                    )
                )
            }
        }

        return events
    }
}
