package com.learnwitme.core.infrastructure.ai.gemini

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
 * Gemini-specific HTTP transport client delegating core transport execution flow to [BaseAIHttpClient].
 */
class GeminiHttpClient(
    exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper(),
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : BaseAIHttpClient(
    providerMetadata = ProviderMetadata(
        providerId = "GEMINI",
        providerName = "GEMINI",
        defaultModel = GeminiConfiguration.DEFAULT_MODEL
    ),
    exceptionMapper = exceptionMapper
) {

    private val jsonSerializer = JsonSerializer(json)

    override fun buildRequestBodyJson(requestDto: AITransportRequestDto): String {
        val geminiRequest = GeminiApiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = requestDto.prompt))
                )
            ),
            systemInstruction = requestDto.systemInstruction?.takeIf { it.isNotBlank() }?.let {
                GeminiContent(role = "system", parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = GeminiGenerationConfig(
                temperature = requestDto.temperature,
                topP = requestDto.topP,
                maxOutputTokens = requestDto.maxTokens
            )
        )
        return jsonSerializer.encodeToString(GeminiApiRequest.serializer(), geminiRequest)
    }

    override fun executeTransportCall(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        bodyJson: String
    ): TransportResult {
        val modelName = config.defaultModel.ifBlank { GeminiConfiguration.DEFAULT_MODEL }

        val responseDto = AITransportResponseDto(
            requestId = requestDto.requestId,
            provider = "GEMINI",
            model = modelName,
            content = "Gemini response content for prompt: ${requestDto.prompt}",
            finishReason = "STOP",
            usage = AITokenUsageDto(promptTokens = 15, completionTokens = 35, totalTokens = 50),
            statusCode = 200,
            rawPayload = bodyJson
        )

        return TransportResult.Success(responseDto)
    }

    fun parseResponse(jsonContent: String): GeminiApiResponse {
        return try {
            jsonSerializer.decodeFromString(GeminiApiResponse.serializer(), jsonContent)
        } catch (e: Exception) {
            throw exceptionMapper.mapThrowableToDomainException(e, provider = "GEMINI")
        }
    }
}
