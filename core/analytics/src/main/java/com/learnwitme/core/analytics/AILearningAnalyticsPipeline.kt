package com.learnwitme.core.analytics

import com.learnwitme.core.application.ai.feedback.AILearningInteractionResult

/**
 * High-level pipeline responsible for mapping [AILearningInteractionResult] objects
 * into structured analytics events and dispatching them via [AILearningAnalyticsDispatcher].
 */
class AILearningAnalyticsPipeline(
    private val dispatcher: AILearningAnalyticsDispatcher,
    private val mapper: AILearningAnalyticsEventMapper = AILearningAnalyticsEventMapper()
) {
    /**
     * Maps an AI interaction result to analytics events and dispatches them atomically.
     * Returns the list of dispatched analytics events.
     */
    suspend fun processAndDispatch(result: AILearningInteractionResult<*>): List<AILearningAnalyticsEvent> {
        val events = mapper.mapToAnalyticsEvents(result)
        dispatcher.dispatchAll(events)
        return events
    }
}
