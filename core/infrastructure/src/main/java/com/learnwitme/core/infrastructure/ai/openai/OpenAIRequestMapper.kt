package com.learnwitme.core.infrastructure.ai.openai

import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIRequestMapper
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto

/**
 * Maps domain-level execution requests and AIRequests to OpenAI Chat Completions DTOs.
 */
class OpenAIRequestMapper : AIRequestMapper<OpenAIChatRequest> {

    override fun mapToTransportRequest(
        request: AIExecutionRequest,
        config: AIProviderConfiguration
    ): OpenAIChatRequest {
        val messages = mutableListOf<OpenAIChatMessage>()

        if (request.systemPrompt.isNotBlank()) {
            messages.add(OpenAIChatMessage(role = "system", content = request.systemPrompt))
        }

        val userPromptWithContext = buildString {
            if (request.context.isNotBlank()) {
                append("[Context]: ").append(request.context).append("\n\n")
            }
            append(request.prompt)
        }
        messages.add(OpenAIChatMessage(role = "user", content = userPromptWithContext))

        val modelName = config.defaultModel.ifBlank { OpenAIConfiguration.DEFAULT_MODEL }

        return OpenAIChatRequest(
            model = modelName,
            messages = messages,
            temperature = request.temperature,
            topP = request.topP,
            maxTokens = request.maxTokens
        )
    }

    fun mapFromAIRequest(request: AIRequest, model: String = OpenAIConfiguration.DEFAULT_MODEL): OpenAIChatRequest {
        val messages = mutableListOf<OpenAIChatMessage>()

        if (request.systemInstruction.isNotBlank()) {
            messages.add(OpenAIChatMessage(role = "system", content = request.systemInstruction))
        }

        val promptText = buildString {
            if (request.context.isNotBlank()) {
                append("[Context]: ").append(request.context).append("\n\n")
            }
            append(request.prompt)
        }
        messages.add(OpenAIChatMessage(role = "user", content = promptText))

        return OpenAIChatRequest(
            model = model,
            messages = messages,
            temperature = request.temperature,
            maxTokens = request.maxTokens
        )
    }

    fun mapToTransportDto(
        request: AIExecutionRequest,
        config: AIProviderConfiguration
    ): AITransportRequestDto {
        return AITransportRequestDto(
            requestId = request.requestId,
            model = config.defaultModel.ifBlank { OpenAIConfiguration.DEFAULT_MODEL },
            prompt = request.prompt,
            systemInstruction = request.systemPrompt,
            temperature = request.temperature,
            topP = request.topP,
            maxTokens = request.maxTokens
        )
    }
}
