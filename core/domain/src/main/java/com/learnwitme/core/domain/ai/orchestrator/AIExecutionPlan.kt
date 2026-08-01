package com.learnwitme.core.domain.ai.orchestrator

import com.learnwitme.core.domain.ai.runtime.AIExecutionContext
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import kotlinx.serialization.Serializable

/**
 * Immutable execution plan governing an AI runtime invocation.
 */
@Serializable
data class AIExecutionPlan(
    val executionId: String,
    val correlationId: String = "",
    val executionRequest: AIExecutionRequest,
    val executionContext: AIExecutionContext = AIExecutionContext.DEFAULT,
    val executionPolicy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
    val preferredProvider: String = executionPolicy.preferredProvider,
    val fallbackProvider: String? = executionPolicy.fallbackProvider,
    val streaming: Boolean = executionRequest.streaming,
    val timeoutMs: Long = executionRequest.timeoutMs,
    val retryPolicy: AIRetryPolicy = AIRetryPolicy.DEFAULT,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(executionId.isNotBlank()) { "executionId cannot be blank" }
        require(preferredProvider.isNotBlank()) { "preferredProvider cannot be blank" }
        require(timeoutMs > 0) { "timeoutMs must be positive" }
    }
}
