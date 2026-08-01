package com.learnwitme.core.infrastructure.ai.openai

import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse
import com.learnwitme.core.domain.ai.runtime.AITokenUsage
import com.learnwitme.core.infrastructure.ai.AIResponseMapper
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto

/**
 * Maps OpenAI REST API response DTOs to Sprint 8 [AIResponse] and Sprint 10 [AIExecutionResponse].
 */
class OpenAIResponseMapper : AIResponseMapper<OpenAIChatResponse> {

    override fun mapToDomainResponse(
        transportResponse: OpenAIChatResponse,
        requestId: String
    ): AIExecutionResponse {
        val choice = transportResponse.choices?.firstOrNull()
        val text = choice?.message?.content ?: ""
        val finishReason = choice?.finishReason ?: "stop"

        val promptTokens = transportResponse.usage?.promptTokens ?: 0
        val completionTokens = transportResponse.usage?.completionTokens ?: 0
        val totalTokens = transportResponse.usage?.totalTokens ?: (promptTokens + completionTokens)

        val tokenUsage = AITokenUsage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens
        )

        return AIExecutionResponse(
            requestId = requestId,
            provider = "OPENAI",
            model = transportResponse.model ?: OpenAIConfiguration.DEFAULT_MODEL,
            content = text,
            finishReason = finishReason,
            tokenUsage = tokenUsage,
            executionTimeMs = 0L
        )
    }

    fun mapToSprint8Response(
        openAIResponse: OpenAIChatResponse,
        modelId: String,
        executionTimeMs: Long
    ): AIResponse {
        val choice = openAIResponse.choices?.firstOrNull()
        val text = choice?.message?.content ?: ""
        val finishReason = choice?.finishReason ?: "stop"
        val totalTokens = openAIResponse.usage?.totalTokens
            ?: ((openAIResponse.usage?.promptTokens ?: 0) + (openAIResponse.usage?.completionTokens ?: 0))

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
            finishReason = dto.finishReason ?: "stop",
            tokenUsage = tokenUsage,
            executionTimeMs = 0L
        )
    }
}
