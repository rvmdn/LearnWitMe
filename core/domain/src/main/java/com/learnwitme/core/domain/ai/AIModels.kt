package com.learnwitme.core.domain.ai

/**
 * Supported AI LLM Provider Types.
 */
enum class AIProviderType {
    GEMINI,
    OPENAI,
    CLAUDE,
    OLLAMA,
    MOCK
}

/**
 * LLM Model descriptor value object.
 */
data class AIModel(
    val id: String,
    val name: String,
    val providerType: AIProviderType,
    val maxTokens: Int = 4096,
    val temperature: Double = 0.7
)

/**
 * AI Provider connection configuration.
 */
data class AIProviderConfiguration(
    val providerType: AIProviderType,
    val apiKey: String = "",
    val baseUrl: String = "",
    val defaultModel: AIModel,
    val timeoutMs: Long = 30000L
)

/**
 * Input request payload for AI text generation.
 */
data class AIRequest(
    val prompt: String,
    val context: String = "",
    val systemInstruction: String = "",
    val maxTokens: Int? = null,
    val temperature: Double? = null
)

/**
 * Unified response payload returned by AI providers.
 */
data class AIResponse(
    val content: String,
    val tokensUsed: Int = 0,
    val finishReason: String = "STOP",
    val modelId: String,
    val executionTimeMs: Long
)
