package com.learnwitme.core.domain.ai.orchestrator

import com.learnwitme.core.domain.ai.runtime.AIExecutionContext
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import kotlinx.serialization.Serializable

/**
 * Pure validator for AI execution requests, contexts, and policies.
 * Exception-safe and deterministic.
 */
class AIExecutionValidator {

    @Serializable
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    ) {
        companion object {
            val SUCCESS = ValidationResult(isValid = true)
        }
    }

    fun validate(
        request: AIExecutionRequest,
        context: AIExecutionContext = AIExecutionContext.DEFAULT,
        policy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
        isProviderAvailable: Boolean = true
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (request.requestId.isBlank()) {
            errors.add("Request requestId cannot be blank")
        }
        if (request.prompt.isBlank()) {
            errors.add("Request prompt cannot be blank")
        }
        if (request.timeoutMs <= 0) {
            errors.add("Request timeoutMs must be positive")
        }
        if (!isProviderAvailable) {
            errors.add("Preferred AI Provider (${policy.preferredProvider}) is unavailable")
        }

        if (policy.allowRetry && policy.maxRetry <= 0) {
            warnings.add("Policy permits retry but maxRetry is 0")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
