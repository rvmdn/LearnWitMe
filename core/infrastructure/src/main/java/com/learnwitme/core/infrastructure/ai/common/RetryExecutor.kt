package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.domain.ai.AIProviderException

/**
 * Pure retry executor for transport requests.
 */
class RetryExecutor {

    fun <T> executeWithRetry(
        maxRetries: Int = 3,
        retryCondition: (Throwable) -> Boolean = { it is AIProviderException.RateLimitException || it is AIProviderException.NetworkTimeoutException },
        block: (attempt: Int) -> T
    ): T {
        var lastException: Throwable? = null
        val attempts = maxRetries.coerceAtLeast(1)

        for (attempt in 1..attempts) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                lastException = t
                if (attempt == attempts || !retryCondition(t)) {
                    throw t
                }
            }
        }
        throw lastException ?: IllegalStateException("Execution failed without exception")
    }
}
