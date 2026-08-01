package com.learnwitme.core.domain.ai.runtime

import com.learnwitme.core.domain.ai.AIProviderException

/**
 * Sealed hierarchy representing execution results from the AI Runtime layer.
 * Pure Kotlin, deterministic result wrapper.
 */
sealed class AIExecutionResult {

    /**
     * Execution succeeded with a response.
     */
    data class Success(
        val response: AIExecutionResponse
    ) : AIExecutionResult()

    /**
     * Execution failed due to a domain-level AI provider exception.
     */
    data class Failure(
        val exception: AIProviderException,
        val requestId: String? = null
    ) : AIExecutionResult()

    /**
     * Execution was cancelled.
     */
    data class Cancelled(
        val requestId: String,
        val reason: String = "Execution cancelled by caller"
    ) : AIExecutionResult()

    /**
     * Execution timed out.
     */
    data class Timeout(
        val requestId: String,
        val timeoutMs: Long,
        val message: String = "Execution timed out after $timeoutMs ms"
    ) : AIExecutionResult()
}
