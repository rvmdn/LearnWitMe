package com.learnwitme.core.domain.ai.orchestrator

import kotlinx.serialization.Serializable

/**
 * Immutable retry policy configuration for AI execution attempts.
 */
@Serializable
data class AIRetryPolicy(
    val maxAttempts: Int = 3,
    val retryDelayMs: Long = 1000L,
    val exponentialBackoff: Boolean = true,
    val retryOnTimeout: Boolean = true,
    val retryOnRateLimit: Boolean = true
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }
        require(retryDelayMs >= 0) { "retryDelayMs must be non-negative" }
    }

    companion object {
        val DEFAULT = AIRetryPolicy()
        val STRICT = AIRetryPolicy(
            maxAttempts = 1,
            retryDelayMs = 0L,
            exponentialBackoff = false,
            retryOnTimeout = false,
            retryOnRateLimit = false
        )
        val NO_RETRY = AIRetryPolicy(
            maxAttempts = 1,
            retryDelayMs = 0L,
            exponentialBackoff = false,
            retryOnTimeout = false,
            retryOnRateLimit = false
        )
    }
}
