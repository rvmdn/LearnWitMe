package com.learnwitme.core.domain.ai.response

import kotlinx.serialization.Serializable

/**
 * Immutable validation result for raw or processed AI responses.
 */
@Serializable
data class ResponseValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    companion object {
        val SUCCESS = ResponseValidationResult(isValid = true)
    }
}
