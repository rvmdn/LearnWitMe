package com.learnwitme.core.infrastructure.ai.openrouter

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AITokenUsageDto
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper
import com.learnwitme.core.infrastructure.ai.common.BaseAIHttpClient
import com.learnwitme.core.infrastructure.ai.common.JsonSerializer
import com.learnwitme.core.infrastructure.ai.common.ProviderMetadata
import com.learnwitme.core.infrastructure.ai.common.TransportResult
import kotlinx.serialization.json.Json

/**
 * OpenRouter-specific HTTP transport client delegating core transport execution flow to [BaseAIHttpClient].
 */
class OpenRouterHttpClient(
    exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper(),
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : BaseAIHttpClient(
    providerMetadata = ProviderMetadata(
        providerId = "OPENROUTER",
        providerName = "OPENROUTER",
        defaultModel = OpenRouterConfiguration.DEFAULT_MODEL
    ),
    exceptionMapper = exceptionMapper
) {

    private val jsonSerializer = JsonSerializer(json)

    override fun buildRequestBodyJson(requestDto: AITransportRequestDto): String {
        val messages = mutableListOf<OpenRouterChatMessage>()

        if (!requestDto.systemInstruction.isNullOrBlank()) {
            messages.add(OpenRouterChatMessage(role = "system", content = requestDto.systemInstruction))
        }
        messages.add(OpenRouterChatMessage(role = "user", content = requestDto.prompt))

        val chatRequest = OpenRouterChatRequest(
            model = requestDto.model.ifBlank { OpenRouterConfiguration.DEFAULT_MODEL },
            messages = messages,
            temperature = requestDto.temperature,
            topP = requestDto.topP,
            maxTokens = requestDto.maxTokens
        )
        return jsonSerializer.encodeToString(OpenRouterChatRequest.serializer(), chatRequest)
    }

    override fun executeTransportCall(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        bodyJson: String
    ): TransportResult {
        val modelName = config.defaultModel.ifBlank { OpenRouterConfiguration.DEFAULT_MODEL }

        val responseDto = AITransportResponseDto(
            requestId = requestDto.requestId,
            provider = "OPENROUTER",
            model = modelName,
            content = "OpenRouter response content for prompt: ${requestDto.prompt}",
            finishReason = "stop",
            usage = AITokenUsageDto(promptTokens = 25, completionTokens = 45, totalTokens = 70),
            statusCode = 200,
            rawPayload = bodyJson
        )

        return TransportResult.Success(responseDto)
    }

    fun parseResponse(jsonContent: String): OpenRouterChatResponse {
        return try {
            jsonSerializer.decodeFromString(OpenRouterChatResponse.serializer(), jsonContent)
        } catch (e: Exception) {
            throw exceptionMapper.mapThrowableToDomainException(e, provider = "OPENROUTER")
        }
    }
}
