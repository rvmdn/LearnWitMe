package com.learnwitme.core.infrastructure.ai.openai

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import kotlinx.serialization.Serializable

/**
 * Infrastructure configuration for OpenAI REST API provider.
 */
@Serializable
data class OpenAIConfiguration(
    val apiKey: String = "",
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMs: Long = 30_000L,
    val maxRetries: Int = 3
) {
    fun toProviderConfiguration(): AIProviderConfiguration {
        return AIProviderConfiguration(
            providerName = "OPENAI",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = model,
            timeoutMs = timeoutMs,
            maxRetries = maxRetries
        )
    }

    companion object {
        const val DEFAULT_MODEL = "gpt-4o-mini"
        const val DEFAULT_BASE_URL = "https://api.openai.com/v1"
    }
}
