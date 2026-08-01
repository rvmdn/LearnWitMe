package com.learnwitme.core.infrastructure.ai.ollama

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import kotlinx.serialization.Serializable

/**
 * Infrastructure configuration for local Ollama REST provider.
 */
@Serializable
data class OllamaConfiguration(
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMs: Long = 60_000L,
    val maxRetries: Int = 2
) {
    fun toProviderConfiguration(): AIProviderConfiguration {
        return AIProviderConfiguration(
            providerName = "OLLAMA",
            baseUrl = baseUrl,
            apiKey = "local_no_key",
            defaultModel = model,
            timeoutMs = timeoutMs,
            maxRetries = maxRetries
        )
    }

    companion object {
        const val DEFAULT_MODEL = "llama3"
        const val DEFAULT_BASE_URL = "http://localhost:11434"
    }
}
