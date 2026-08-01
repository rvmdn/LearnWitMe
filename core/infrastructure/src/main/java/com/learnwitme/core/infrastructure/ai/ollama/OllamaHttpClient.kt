package com.learnwitme.core.infrastructure.ai.ollama

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AITokenUsageDto
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper
import com.learnwitme.core.infrastructure.ai.common.BaseAIHttpClient
import com.learnwitme.core.infrastructure.ai.common.JsonSerializer
import com.learnwitme.core.infrastructure.ai.common.ProviderCapability
import com.learnwitme.core.infrastructure.ai.common.ProviderMetadata
import com.learnwitme.core.infrastructure.ai.common.TransportResult
import kotlinx.serialization.json.Json

/**
 * Ollama-specific HTTP transport client delegating core transport execution flow to [BaseAIHttpClient].
 */
class OllamaHttpClient(
    exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper(),
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : BaseAIHttpClient(
    providerMetadata = ProviderMetadata(
        providerId = "OLLAMA",
        providerName = "OLLAMA",
        defaultModel = OllamaConfiguration.DEFAULT_MODEL,
        capabilities = ProviderCapability(supportsOffline = true)
    ),
    exceptionMapper = exceptionMapper
) {

    private val jsonSerializer = JsonSerializer(json)

    override fun buildRequestBodyJson(requestDto: AITransportRequestDto): String {
        val ollamaRequest = OllamaGenerateRequest(
            model = requestDto.model.ifBlank { OllamaConfiguration.DEFAULT_MODEL },
            prompt = requestDto.prompt,
            system = requestDto.systemInstruction,
            stream = false,
            options = OllamaOptions(
                temperature = requestDto.temperature,
                topP = requestDto.topP,
                numPredict = requestDto.maxTokens
            )
        )
        return jsonSerializer.encodeToString(OllamaGenerateRequest.serializer(), ollamaRequest)
    }

    override fun executeTransportCall(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        bodyJson: String
    ): TransportResult {
        val modelName = config.defaultModel.ifBlank { OllamaConfiguration.DEFAULT_MODEL }

        val responseDto = AITransportResponseDto(
            requestId = requestDto.requestId,
            provider = "OLLAMA",
            model = modelName,
            content = "Ollama local response content for prompt: ${requestDto.prompt}",
            finishReason = "stop",
            usage = AITokenUsageDto(promptTokens = 10, completionTokens = 30, totalTokens = 40),
            statusCode = 200,
            rawPayload = bodyJson
        )

        return TransportResult.Success(responseDto)
    }

    fun parseResponse(jsonContent: String): OllamaGenerateResponse {
        return try {
            jsonSerializer.decodeFromString(OllamaGenerateResponse.serializer(), jsonContent)
        } catch (e: Exception) {
            throw exceptionMapper.mapThrowableToDomainException(e, provider = "OLLAMA")
        }
    }
}
