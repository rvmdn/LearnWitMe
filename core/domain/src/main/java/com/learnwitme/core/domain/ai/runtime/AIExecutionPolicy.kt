package com.learnwitme.core.domain.ai.runtime

import kotlinx.serialization.Serializable

/**
 * Immutable execution policy for configuring runtime behavior options.
 */
@Serializable
data class AIExecutionPolicy(
    val allowStreaming: Boolean = false,
    val allowFallback: Boolean = true,
    val allowRetry: Boolean = true,
    val maxRetry: Int = 3,
    val timeoutMs: Long = 30_000L,
    val privacyLevel: String = "STANDARD",
    val preferredProvider: String = "GEMINI",
    val fallbackProvider: String? = "OPENAI"
) {
    init {
        require(maxRetry >= 0) { "maxRetry must be non-negative" }
        require(timeoutMs > 0) { "timeoutMs must be positive" }
    }

    companion object {
        val DEFAULT = AIExecutionPolicy()
        val STRICT = AIExecutionPolicy(
            allowStreaming = false,
            allowFallback = false,
            allowRetry = false,
            maxRetry = 0,
            timeoutMs = 15_000L,
            privacyLevel = "STRICT"
        )
    }
}
