package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIRequestMapper
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto

/**
 * Maps domain-level execution requests and AIRequests to Gemini API transport DTOs.
 */
class GeminiRequestMapper : AIRequestMapper<GeminiApiRequest> {

    override fun mapToTransportRequest(
        request: AIExecutionRequest,
        config: AIProviderConfiguration
    ): GeminiApiRequest {
        val userPromptWithContext = buildString {
            if (request.context.isNotBlank()) {
                append("[Context]: ").append(request.context).append("\n\n")
            }
            append(request.prompt)
        }

        val userContent = GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = userPromptWithContext))
        )

        val systemContent = if (request.systemPrompt.isNotBlank()) {
            GeminiContent(
                role = "system",
                parts = listOf(GeminiPart(text = request.systemPrompt))
            )
        } else null

        val genConfig = GeminiGenerationConfig(
            temperature = request.temperature,
            topP = request.topP,
            maxOutputTokens = request.maxTokens
        )

        return GeminiApiRequest(
            contents = listOf(userContent),
            systemInstruction = systemContent,
            generationConfig = genConfig
        )
    }

    fun mapFromAIRequest(request: AIRequest): GeminiApiRequest {
        val promptText = buildString {
            if (request.context.isNotBlank()) {
                append("[Context]: ").append(request.context).append("\n\n")
            }
            append(request.prompt)
        }

        val userContent = GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = promptText))
        )

        val systemContent = if (request.systemInstruction.isNotBlank()) {
            GeminiContent(
                role = "system",
                parts = listOf(GeminiPart(text = request.systemInstruction))
            )
        } else null

        val genConfig = GeminiGenerationConfig(
            temperature = request.temperature,
            maxOutputTokens = request.maxTokens
        )

        return GeminiApiRequest(
            contents = listOf(userContent),
            systemInstruction = systemContent,
            generationConfig = genConfig
        )
    }

    fun mapToTransportDto(
        request: AIExecutionRequest,
        config: AIProviderConfiguration
    ): AITransportRequestDto {
        return AITransportRequestDto(
            requestId = request.requestId,
            model = config.defaultModel.ifBlank { GeminiConfiguration.DEFAULT_MODEL },
            prompt = request.prompt,
            systemInstruction = request.systemPrompt,
            temperature = request.temperature,
            topP = request.topP,
            maxTokens = request.maxTokens
        )
    }
}
