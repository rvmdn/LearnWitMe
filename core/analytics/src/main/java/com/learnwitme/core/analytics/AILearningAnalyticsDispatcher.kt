package com.learnwitme.core.analytics

/**
 * Framework-independent contract for dispatching learning analytics events.
 *
 * Implementations may buffer, log, or forward events to external analytics infrastructure.
 */
interface AILearningAnalyticsDispatcher {
    /**
     * Dispatches a single learning analytics event.
     */
    suspend fun dispatch(event: AILearningAnalyticsEvent)

    /**
     * Dispatches multiple learning analytics events.
     */
    suspend fun dispatchAll(events: List<AILearningAnalyticsEvent>) {
        events.forEach { dispatch(it) }
    }
}

/**
 * Thread-safe, in-memory implementation of [AILearningAnalyticsDispatcher] for unit testing and local development.
 */
class InMemoryAILearningAnalyticsDispatcher : AILearningAnalyticsDispatcher {
    private val _events = mutableListOf<AILearningAnalyticsEvent>()
    val events: List<AILearningAnalyticsEvent>
        get() = synchronized(_events) { _events.toList() }

    override suspend fun dispatch(event: AILearningAnalyticsEvent) {
        synchronized(_events) {
            _events.add(event)
        }
    }

    fun clear() {
        synchronized(_events) {
            _events.clear()
        }
    }
}
