package com.learnwitme.core.infrastructure.ai.ollama

import com.learnwitme.core.domain.ai.AIProvider
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse
import com.learnwitme.core.domain.ai.runtime.AITokenUsage
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper

/**
 * Concrete implementation of domain [AIProvider] interface for local Ollama instances.
 */
class OllamaProvider(
    private val config: OllamaConfiguration = OllamaConfiguration(),
    private val httpClient: OllamaHttpClient = OllamaHttpClient(),
    private val exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper()
) : AIProvider {

    override suspend fun generateExplanation(request: AIRequest): AIResponse {
        return executeAIRequest(request, "Explain in detail:")
    }

    override suspend fun generateLesson(request: AIRequest): AIResponse {
        return executeAIRequest(request, "Generate a lesson for:")
    }

    override suspend fun generateQuiz(request: AIRequest): AIResponse {
        return executeAIRequest(request, "Generate a quiz for:")
    }

    override suspend fun simplifyContent(request: AIRequest): AIResponse {
        return executeAIRequest(request, "Simplify content:")
    }

    fun executeRuntimeRequest(request: AIExecutionRequest): AIExecutionResponse {
        val startTime = System.currentTimeMillis()
        try {
            val providerConfig = config.toProviderConfiguration()
            val transportDto = com.learnwitme.core.infrastructure.ai.AITransportRequestDto(
                requestId = request.requestId,
                model = providerConfig.defaultModel.ifBlank { OllamaConfiguration.DEFAULT_MODEL },
                prompt = request.prompt,
                systemInstruction = request.systemPrompt,
                temperature = request.temperature,
                topP = request.topP,
                maxTokens = request.maxTokens
            )

            val transportResponse = httpClient.execute(providerConfig, transportDto)
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(0L)

            val usage = transportResponse.usage
            val tokenUsage = if (usage != null) {
                AITokenUsage(usage.promptTokens, usage.completionTokens, usage.totalTokens)
            } else AITokenUsage.ZERO

            return AIExecutionResponse(
                requestId = transportResponse.requestId,
                provider = "OLLAMA",
                model = transportResponse.model,
                content = transportResponse.content,
                finishReason = transportResponse.finishReason ?: "stop",
                tokenUsage = tokenUsage,
                executionTimeMs = duration
            )
        } catch (t: Throwable) {
            throw exceptionMapper.mapThrowableToDomainException(t, provider = "OLLAMA")
        }
    }

    private fun executeAIRequest(request: AIRequest, prefix: String): AIResponse {
        val startTime = System.currentTimeMillis()
        try {
            val providerConfig = config.toProviderConfiguration()
            val transportDto = com.learnwitme.core.infrastructure.ai.AITransportRequestDto(
                requestId = "req_${System.currentTimeMillis()}",
                model = providerConfig.defaultModel,
                prompt = "$prefix ${request.prompt}",
                systemInstruction = request.systemInstruction,
                temperature = request.temperature ?: 0.7,
                maxTokens = request.maxTokens ?: 2048
            )

            val transportResponse = httpClient.execute(providerConfig, transportDto)
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(0L)

            return AIResponse(
                content = transportResponse.content,
                tokensUsed = transportResponse.usage?.totalTokens ?: 0,
                finishReason = transportResponse.finishReason ?: "stop",
                modelId = transportResponse.model,
                executionTimeMs = duration
            )
        } catch (t: Throwable) {
            throw exceptionMapper.mapThrowableToDomainException(t, provider = "OLLAMA")
        }
    }
}
