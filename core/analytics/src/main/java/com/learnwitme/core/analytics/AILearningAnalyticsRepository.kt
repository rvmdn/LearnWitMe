package com.learnwitme.core.analytics

/**
 * State of a queued analytics event within the persistent storage.
 */
enum class AnalyticsDeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED
}

/**
 * Persistent queued analytics event record with delivery tracking metadata.
 */
data class QueuedAnalyticsEvent(
    val event: AILearningAnalyticsEvent,
    val deliveryStatus: AnalyticsDeliveryStatus,
    val attemptCount: Int,
    val lastAttemptAt: Long?,
    val nextRetryAt: Long?,
    val errorMessage: String?
)

/**
 * Contract for persistent analytics event queue operations.
 *
 * Designed for offline-first operation, idempotent queuing, and reliable delivery boundaries.
 */
interface AILearningAnalyticsRepository {
    /**
     * Enqueues analytics events into persistent storage.
     * Enforces idempotency via [AILearningAnalyticsEvent.eventId].
     */
    suspend fun enqueue(events: List<AILearningAnalyticsEvent>)

    /**
     * Retrieves pending analytics events ready for delivery.
     */
    suspend fun getPendingEvents(limit: Int = 100): List<QueuedAnalyticsEvent>

    /**
     * Marks events as successfully delivered.
     */
    suspend fun markDelivered(eventIds: List<String>)

    /**
     * Marks an event delivery attempt as failed and updates retry metadata.
     */
    suspend fun markFailed(eventId: String, errorMessage: String?, nextRetryAt: Long? = null)

    /**
     * Returns total count of pending events in queue.
     */
    suspend fun getPendingCount(): Int
}
