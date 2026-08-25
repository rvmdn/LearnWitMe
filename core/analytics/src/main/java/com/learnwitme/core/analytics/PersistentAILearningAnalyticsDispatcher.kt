package com.learnwitme.core.analytics

/**
 * Reliable, persistent implementation of [AILearningAnalyticsDispatcher].
 *
 * Principles:
 * - Enqueues analytics events into persistent storage ([AILearningAnalyticsRepository]) for offline-first support.
 * - Attempts immediate delivery to downstream [delegate] if connected.
 * - Fault Isolation: Exceptions during persistence or delivery are safely caught to ensure
 *   learning and domain activities are never disrupted by analytics failures.
 */
class PersistentAILearningAnalyticsDispatcher(
    private val repository: AILearningAnalyticsRepository,
    private val delegate: AILearningAnalyticsDispatcher? = null
) : AILearningAnalyticsDispatcher {

    override suspend fun dispatch(event: AILearningAnalyticsEvent) {
        dispatchAll(listOf(event))
    }

    override suspend fun dispatchAll(events: List<AILearningAnalyticsEvent>) {
        if (events.isEmpty()) return
        try {
            // 1. Enqueue to offline-first persistent storage
            repository.enqueue(events)

            // 2. Attempt immediate delivery if delegate is present
            delegate?.let { downstream ->
                for (event in events) {
                    try {
                        downstream.dispatch(event)
                        repository.markDelivered(listOf(event.eventId))
                    } catch (e: Exception) {
                        repository.markFailed(
                            eventId = event.eventId,
                            errorMessage = e.message ?: "Delivery failed"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Analytics Failure Isolation: log/suppress to prevent domain errors
        }
    }
}
