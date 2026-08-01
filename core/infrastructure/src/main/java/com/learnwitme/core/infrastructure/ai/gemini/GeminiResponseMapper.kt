package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse
import com.learnwitme.core.domain.ai.runtime.AITokenUsage
import com.learnwitme.core.infrastructure.ai.AIResponseMapper
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto

/**
 * Maps Gemini REST API response DTOs to Sprint 8 [AIResponse] and Sprint 10 [AIExecutionResponse].
 */
class GeminiResponseMapper : AIResponseMapper<GeminiApiResponse> {

    override fun mapToDomainResponse(
        transportResponse: GeminiApiResponse,
        requestId: String
    ): AIExecutionResponse {
        val candidate = transportResponse.candidates?.firstOrNull()
        val text = candidate?.content?.parts?.joinToString("\n") { it.text } ?: ""
        val finishReason = candidate?.finishReason ?: "STOP"

        val promptTokens = transportResponse.usageMetadata?.promptTokenCount ?: 0
        val completionTokens = transportResponse.usageMetadata?.candidatesTokenCount ?: 0
        val totalTokens = transportResponse.usageMetadata?.totalTokenCount ?: (promptTokens + completionTokens)

        val tokenUsage = AITokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens
        )

        return AIExecutionResponse(
            requestId = requestId,
            provider = "GEMINI",
            model = GeminiConfiguration.DEFAULT_MODEL,
            content = text,
            finishReason = finishReason,
            tokenUsage = tokenUsage,
            executionTimeMs = 0L
        )
    }

    fun mapToSprint8Response(
        geminiResponse: GeminiApiResponse,
        modelId: String,
        executionTimeMs: Long
    ): AIResponse {
        val candidate = geminiResponse.candidates?.firstOrNull()
        val text = candidate?.content?.parts?.joinToString("\n") { it.text } ?: ""
        val finishReason = candidate?.finishReason ?: "STOP"
        val totalTokens = geminiResponse.usageMetadata?.totalTokenCount
            ?: ((geminiResponse.usageMetadata?.promptTokenCount ?: 0) + (geminiResponse.usageMetadata?.candidatesTokenCount ?: 0))

        return AIResponse(
            content = text,
            tokensUsed = totalTokens,
            finishReason = finishReason,
            modelId = modelId,
            executionTimeMs = executionTimeMs
        )
    }

    fun mapFromTransportDto(
        dto: AITransportResponseDto
    ): AIExecutionResponse {
        val usage = dto.usage
        val tokenUsage = if (usage != null) {
            AITokenUsage(
                promptTokens = usage.promptTokens,
                completionTokens = usage.completionTokens,
                totalTokens = usage.totalTokens
            )
        } else {
            AITokenUsage.ZERO
        }

        return AIExecutionResponse(
            requestId = dto.requestId,
            provider = dto.provider,
            model = dto.model,
            content = dto.content,
            finishReason = dto.finishReason ?: "STOP",
            tokenUsage = tokenUsage,
            executionTimeMs = 0L
        )
    }
}
