package com.learnwitme.core.infrastructure.ai.openrouter

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import kotlinx.serialization.Serializable

/**
 * Infrastructure configuration for OpenRouter REST API provider.
 */
@Serializable
data class OpenRouterConfiguration(
    val apiKey: String = "",
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMs: Long = 30_000L,
    val maxRetries: Int = 3
) {
    fun toProviderConfiguration(): AIProviderConfiguration {
        return AIProviderConfiguration(
            providerName = "OPENROUTER",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = model,
            timeoutMs = timeoutMs,
            maxRetries = maxRetries
        )
    }

    companion object {
        const val DEFAULT_MODEL = "anthropic/claude-3.5-sonnet"
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
    }
}
