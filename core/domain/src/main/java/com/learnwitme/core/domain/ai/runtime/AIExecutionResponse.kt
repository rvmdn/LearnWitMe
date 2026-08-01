package com.learnwitme.core.domain.ai.runtime

import kotlinx.serialization.Serializable

/**
 * Immutable execution response returned by the AI Runtime.
 * Pure Kotlin value object; does not expose provider-specific SDK objects.
 */
@Serializable
data class AIExecutionResponse(
    val requestId: String,
    val provider: String,
    val model: String,
    val content: String,
    val finishReason: String = "STOP",
    val tokenUsage: AITokenUsage = AITokenUsage.ZERO,
    val executionTimeMs: Long = 0L,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(requestId.isNotBlank()) { "requestId cannot be blank" }
        require(provider.isNotBlank()) { "provider cannot be blank" }
        require(model.isNotBlank()) { "model cannot be blank" }
        require(executionTimeMs >= 0) { "executionTimeMs must be non-negative" }
    }
}
