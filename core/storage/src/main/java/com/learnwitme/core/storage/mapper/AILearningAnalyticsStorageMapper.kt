package com.learnwitme.core.storage.mapper

import com.learnwitme.core.analytics.AILearningAnalyticsEvent
import com.learnwitme.core.analytics.AnalyticsDeliveryStatus
import com.learnwitme.core.analytics.QueuedAnalyticsEvent
import com.learnwitme.core.storage.local.entity.AILearningAnalyticsEventEntity

/**
 * Mapper converting between domain analytics events ([AILearningAnalyticsEvent])
 * and database entities ([AILearningAnalyticsEventEntity]).
 */
object AILearningAnalyticsStorageMapper {

    fun toEntity(
        event: AILearningAnalyticsEvent,
        deliveryStatus: AnalyticsDeliveryStatus = AnalyticsDeliveryStatus.PENDING,
        attemptCount: Int = 0,
        lastAttemptAt: Long? = null,
        nextRetryAt: Long? = null,
        errorMessage: String? = null
    ): AILearningAnalyticsEventEntity {
        return when (event) {
            is AILearningAnalyticsEvent.InteractionCompleted -> AILearningAnalyticsEventEntity(
                eventId = event.eventId,
                eventType = "INTERACTION_COMPLETED",
                interactionId = event.interactionId,
                userId = event.userId,
                action = event.action,
                timestamp = event.timestamp,
                cardId = event.cardId,
                topicCode = event.topicCode,
                provider = event.provider,
                model = event.model,
                promptTokens = event.promptTokens,
                completionTokens = event.completionTokens,
                totalTokens = event.totalTokens,
                personalizationPreset = event.personalizationPreset,
                outcomeStatus = null,
                summary = null,
                usefulnessRating = null,
                confidenceSignal = null,
                hasComment = false,
                classification = null,
                confidenceDelta = null,
                deliveryStatus = deliveryStatus.name,
                attemptCount = attemptCount,
                lastAttemptAt = lastAttemptAt,
                nextRetryAt = nextRetryAt,
                errorMessage = errorMessage
            )

            is AILearningAnalyticsEvent.OutcomeRecorded -> AILearningAnalyticsEventEntity(
                eventId = event.eventId,
                eventType = "OUTCOME_RECORDED",
                interactionId = event.interactionId,
                userId = event.userId,
                action = event.action,
                timestamp = event.timestamp,
                cardId = null,
                topicCode = null,
                provider = null,
                model = null,
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = 0,
                personalizationPreset = null,
                outcomeStatus = event.outcomeStatus,
                summary = event.summary,
                usefulnessRating = null,
                confidenceSignal = null,
                hasComment = false,
                classification = null,
                confidenceDelta = null,
                deliveryStatus = deliveryStatus.name,
                attemptCount = attemptCount,
                lastAttemptAt = lastAttemptAt,
                nextRetryAt = nextRetryAt,
                errorMessage = errorMessage
            )

            is AILearningAnalyticsEvent.FeedbackRecorded -> AILearningAnalyticsEventEntity(
                eventId = event.eventId,
                eventType = "FEEDBACK_RECORDED",
                interactionId = event.interactionId,
                userId = event.userId,
                action = event.action,
                timestamp = event.timestamp,
                cardId = null,
                topicCode = null,
                provider = null,
                model = null,
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = 0,
                personalizationPreset = null,
                outcomeStatus = null,
                summary = null,
                usefulnessRating = event.usefulnessRating,
                confidenceSignal = event.confidenceSignal,
                hasComment = event.hasComment,
                classification = null,
                confidenceDelta = null,
                deliveryStatus = deliveryStatus.name,
                attemptCount = attemptCount,
                lastAttemptAt = lastAttemptAt,
                nextRetryAt = nextRetryAt,
                errorMessage = errorMessage
            )

            is AILearningAnalyticsEvent.MemoryImpactRecorded -> AILearningAnalyticsEventEntity(
                eventId = event.eventId,
                eventType = "MEMORY_IMPACT_RECORDED",
                interactionId = event.interactionId,
                userId = event.userId,
                action = event.action,
                timestamp = event.timestamp,
                cardId = event.cardId,
                topicCode = event.topicCode,
                provider = null,
                model = null,
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = 0,
                personalizationPreset = null,
                outcomeStatus = null,
                summary = null,
                usefulnessRating = null,
                confidenceSignal = null,
                hasComment = false,
                classification = event.classification,
                confidenceDelta = event.confidenceDelta,
                deliveryStatus = deliveryStatus.name,
                attemptCount = attemptCount,
                lastAttemptAt = lastAttemptAt,
                nextRetryAt = nextRetryAt,
                errorMessage = errorMessage
            )
        }
    }

    fun toQueuedEvent(entity: AILearningAnalyticsEventEntity): QueuedAnalyticsEvent {
        val event: AILearningAnalyticsEvent = when (entity.eventType) {
            "INTERACTION_COMPLETED" -> AILearningAnalyticsEvent.InteractionCompleted(
                eventId = entity.eventId,
                timestamp = entity.timestamp,
                interactionId = entity.interactionId,
                userId = entity.userId,
                action = entity.action,
                cardId = entity.cardId,
                topicCode = entity.topicCode,
                provider = entity.provider,
                model = entity.model,
                promptTokens = entity.promptTokens,
                completionTokens = entity.completionTokens,
                totalTokens = entity.totalTokens,
                personalizationPreset = entity.personalizationPreset
            )

            "OUTCOME_RECORDED" -> AILearningAnalyticsEvent.OutcomeRecorded(
                eventId = entity.eventId,
                timestamp = entity.timestamp,
                interactionId = entity.interactionId,
                userId = entity.userId,
                action = entity.action,
                outcomeStatus = entity.outcomeStatus ?: "UNKNOWN",
                summary = entity.summary
            )

            "FEEDBACK_RECORDED" -> AILearningAnalyticsEvent.FeedbackRecorded(
                eventId = entity.eventId,
                timestamp = entity.timestamp,
                interactionId = entity.interactionId,
                userId = entity.userId,
                action = entity.action,
                usefulnessRating = entity.usefulnessRating ?: 0,
                confidenceSignal = entity.confidenceSignal,
                hasComment = entity.hasComment
            )

            "MEMORY_IMPACT_RECORDED" -> AILearningAnalyticsEvent.MemoryImpactRecorded(
                eventId = entity.eventId,
                timestamp = entity.timestamp,
                interactionId = entity.interactionId,
                userId = entity.userId,
                action = entity.action,
                classification = entity.classification ?: "NEUTRAL_ENGAGEMENT",
                confidenceDelta = entity.confidenceDelta ?: 0.0,
                cardId = entity.cardId,
                topicCode = entity.topicCode
            )

            else -> throw IllegalArgumentException("Unknown event type: ${entity.eventType}")
        }

        return QueuedAnalyticsEvent(
            event = event,
            deliveryStatus = AnalyticsDeliveryStatus.valueOf(entity.deliveryStatus),
            attemptCount = entity.attemptCount,
            lastAttemptAt = entity.lastAttemptAt,
            nextRetryAt = entity.nextRetryAt,
            errorMessage = entity.errorMessage
        )
    }
}
