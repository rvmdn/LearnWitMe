package com.learnwitme.core.domain.ai.runtime

import kotlinx.serialization.Serializable

/**
 * Immutable execution request model submitted to the AI Runtime.
 */
@Serializable
data class AIExecutionRequest(
    val requestId: String,
    val userId: String = "",
    val prompt: String,
    val systemPrompt: String = "",
    val context: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val temperature: Double = 0.7,
    val topP: Double = 1.0,
    val maxTokens: Int = 2048,
    val timeoutMs: Long = 30_000L,
    val streaming: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    init {
        require(requestId.isNotBlank()) { "requestId cannot be blank" }
        require(prompt.isNotBlank()) { "prompt cannot be blank" }
        require(temperature in 0.0..2.0) { "temperature must be between 0.0 and 2.0" }
        require(topP in 0.0..1.0) { "topP must be between 0.0 and 1.0" }
        require(maxTokens > 0) { "maxTokens must be positive" }
        require(timeoutMs > 0) { "timeoutMs must be positive" }
    }
}
