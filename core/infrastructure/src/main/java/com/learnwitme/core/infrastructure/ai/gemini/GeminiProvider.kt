package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.domain.ai.AIProvider
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper

/**
 * Concrete implementation of the domain [AIProvider] interface using Google Gemini LLM backend.
 */
class GeminiProvider(
    private val geminiConfig: GeminiConfiguration = GeminiConfiguration(),
    private val httpClient: GeminiHttpClient = GeminiHttpClient(),
    private val requestMapper: GeminiRequestMapper = GeminiRequestMapper(),
    private val responseMapper: GeminiResponseMapper = GeminiResponseMapper(),
    private val exceptionMapper: ProviderExceptionMapper = ProviderExceptionMapper()
) : AIProvider {

    override suspend fun generateExplanation(request: AIRequest): AIResponse {
        return executeAIRequest(request, prefix = "Explain in detail:")
    }

    override suspend fun generateLesson(request: AIRequest): AIResponse {
        return executeAIRequest(request, prefix = "Generate a comprehensive lesson for:")
    }

    override suspend fun generateQuiz(request: AIRequest): AIResponse {
        return executeAIRequest(request, prefix = "Generate a quiz with multiple choice questions for:")
    }

    override suspend fun simplifyContent(request: AIRequest): AIResponse {
        return executeAIRequest(request, prefix = "Simplify the following content for beginners:")
    }

    /**
     * Executes Stage 1 [AIExecutionRequest] returning Sprint 10 [AIExecutionResponse].
     */
    fun executeRuntimeRequest(request: AIExecutionRequest): AIExecutionResponse {
        val startTime = System.currentTimeMillis()
        try {
            val providerConfig = geminiConfig.toProviderConfiguration()
            val transportDto = requestMapper.mapToTransportDto(request, providerConfig)
            val transportResponse = httpClient.execute(providerConfig, transportDto)
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(0L)

            val domainResponse = responseMapper.mapFromTransportDto(transportResponse)
            return domainResponse.copy(executionTimeMs = duration)
        } catch (t: Throwable) {
            throw exceptionMapper.mapThrowableToDomainException(t, provider = "GEMINI")
        }
    }

    private fun executeAIRequest(request: AIRequest, prefix: String): AIResponse {
        val startTime = System.currentTimeMillis()
        try {
            val fullPrompt = "$prefix ${request.prompt}"
            val modifiedRequest = request.copy(prompt = fullPrompt)
            val providerConfig = geminiConfig.toProviderConfiguration()

            val transportDto = requestMapper.mapToTransportDto(
                request = AIExecutionRequest(
                    requestId = "req_${System.currentTimeMillis()}",
                    prompt = modifiedRequest.prompt,
                    systemPrompt = modifiedRequest.systemInstruction,
                    context = modifiedRequest.context,
                    temperature = modifiedRequest.temperature ?: 0.7,
                    maxTokens = modifiedRequest.maxTokens ?: 2048
                ),
                config = providerConfig
            )

            val transportResponse = httpClient.execute(providerConfig, transportDto)
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(0L)

            return AIResponse(
                content = transportResponse.content,
                tokensUsed = transportResponse.usage?.totalTokens ?: 0,
                finishReason = transportResponse.finishReason ?: "STOP",
                modelId = transportResponse.model,
                executionTimeMs = duration
            )
        } catch (t: Throwable) {
            throw exceptionMapper.mapThrowableToDomainException(t, provider = "GEMINI")
        }
    }
}
