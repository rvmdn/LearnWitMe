package com.learnwitme.core.domain.ai.orchestrator

import kotlinx.serialization.Serializable

/**
 * Immutable execution audit record capturing final execution lifecycle analytics.
 */
@Serializable
data class AIExecutionAudit(
    val executionId: String,
    val correlationId: String = "",
    val provider: String,
    val model: String = "",
    val attempts: Int = 1,
    val executionDurationMs: Long = 0L,
    val finalStatus: AIExecutionStatus,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(executionId.isNotBlank()) { "executionId cannot be blank" }
        require(attempts >= 0) { "attempts must be non-negative" }
        require(executionDurationMs >= 0) { "executionDurationMs must be non-negative" }
    }
}
