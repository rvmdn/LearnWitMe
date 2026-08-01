package com.learnwitme.core.infrastructure.ai

import kotlinx.serialization.Serializable

/**
 * Common DTO representing transport-level request payload sent to AI providers.
 */
@Serializable
data class AITransportRequestDto(
    val requestId: String,
    val model: String,
    val prompt: String,
    val systemInstruction: String? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxTokens: Int? = null,
    val headers: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Common DTO representing raw transport-level response payload received from AI providers.
 */
@Serializable
data class AITransportResponseDto(
    val requestId: String,
    val provider: String,
    val model: String,
    val content: String,
    val finishReason: String? = null,
    val usage: AITokenUsageDto? = null,
    val statusCode: Int = 200,
    val headers: Map<String, String> = emptyMap(),
    val rawPayload: String? = null
)

/**
 * Common DTO for transport-level token usage metrics.
 */
@Serializable
data class AITokenUsageDto(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = promptTokens + completionTokens
)

/**
 * Common DTO for transport-level error response details.
 */
@Serializable
data class AIErrorResponseDto(
    val errorCode: String,
    val errorMessage: String,
    val statusCode: Int = 500,
    val provider: String? = null,
    val rawResponseBody: String? = null
)
