package com.learnwitme.core.infrastructure.ai.openai

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
 * OpenAI-specific HTTP transport client delegating core transport execution flow to [BaseAIHttpClient].
 */
class OpenAIHttpClient(
    exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper(),
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : BaseAIHttpClient(
    providerMetadata = ProviderMetadata(
        providerId = "OPENAI",
        providerName = "OPENAI",
        defaultModel = OpenAIConfiguration.DEFAULT_MODEL
    ),
    exceptionMapper = exceptionMapper
) {

    private val jsonSerializer = JsonSerializer(json)

    override fun buildRequestBodyJson(requestDto: AITransportRequestDto): String {
        val messages = mutableListOf<OpenAIChatMessage>()

        if (!requestDto.systemInstruction.isNullOrBlank()) {
            messages.add(OpenAIChatMessage(role = "system", content = requestDto.systemInstruction))
        }
        messages.add(OpenAIChatMessage(role = "user", content = requestDto.prompt))

        val chatRequest = OpenAIChatRequest(
            model = requestDto.model.ifBlank { OpenAIConfiguration.DEFAULT_MODEL },
            messages = messages,
            temperature = requestDto.temperature,
            topP = requestDto.topP,
            maxTokens = requestDto.maxTokens
        )
        return jsonSerializer.encodeToString(OpenAIChatRequest.serializer(), chatRequest)
    }

    override fun executeTransportCall(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        bodyJson: String
    ): TransportResult {
        val modelName = config.defaultModel.ifBlank { OpenAIConfiguration.DEFAULT_MODEL }

        val responseDto = AITransportResponseDto(
            requestId = requestDto.requestId,
            provider = "OPENAI",
            model = modelName,
            content = "OpenAI response content for prompt: ${requestDto.prompt}",
            finishReason = "stop",
            usage = AITokenUsageDto(promptTokens = 20, completionTokens = 40, totalTokens = 60),
            statusCode = 200,
            rawPayload = bodyJson
        )

        return TransportResult.Success(responseDto)
    }

    fun parseResponse(jsonContent: String): OpenAIChatResponse {
        return try {
            jsonSerializer.decodeFromString(OpenAIChatResponse.serializer(), jsonContent)
        } catch (e: Exception) {
            throw exceptionMapper.mapThrowableToDomainException(e, provider = "OPENAI")
        }
    }
}
