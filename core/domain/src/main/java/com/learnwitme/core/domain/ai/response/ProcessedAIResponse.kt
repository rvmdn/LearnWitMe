package com.learnwitme.core.domain.ai.response

import kotlinx.serialization.Serializable

/**
 * Immutable container for a fully cleaned, normalized, and validated AI response.
 */
@Serializable
data class ProcessedAIResponse(
    val cleanContent: String,
    val metadata: ResponseMetadata = ResponseMetadata(),
    val warnings: List<String> = emptyList(),
    val validationResult: ResponseValidationResult = ResponseValidationResult.SUCCESS,
    val extractedJson: String? = null
)
