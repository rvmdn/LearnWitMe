package com.learnwitme.core.domain.ai.orchestrator

import com.learnwitme.core.domain.ai.runtime.AIExecutionContext
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest

/**
 * Pure deterministic planner that constructs an [AIExecutionPlan] from execution parameters.
 */
class AIExecutionPlanner {

    fun createPlan(
        executionId: String,
        request: AIExecutionRequest,
        context: AIExecutionContext = AIExecutionContext.DEFAULT,
        policy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
        retryPolicy: AIRetryPolicy = AIRetryPolicy.DEFAULT,
        correlationId: String = ""
    ): AIExecutionPlan {
        val mergedMetadata = request.metadata.toMutableMap()
        if (correlationId.isNotBlank()) {
            mergedMetadata["correlationId"] = correlationId
        }

        return AIExecutionPlan(
            executionId = executionId,
            correlationId = correlationId,
            executionRequest = request,
            executionContext = context,
            executionPolicy = policy,
            preferredProvider = policy.preferredProvider,
            fallbackProvider = policy.fallbackProvider,
            streaming = request.streaming,
            timeoutMs = request.timeoutMs,
            retryPolicy = retryPolicy,
            metadata = mergedMetadata
        )
    }
}
