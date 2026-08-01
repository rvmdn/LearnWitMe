package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIProviderFactory

/**
 * Factory for creating configured [GeminiProvider] instances.
 */
class GeminiProviderFactory : AIProviderFactory<GeminiProvider> {

    override fun createProvider(config: AIProviderConfiguration): GeminiProvider {
        val geminiConfig = GeminiConfiguration(
            apiKey = config.apiKey,
            model = config.defaultModel.ifBlank { GeminiConfiguration.DEFAULT_MODEL },
            baseUrl = config.baseUrl.ifBlank { GeminiConfiguration.DEFAULT_BASE_URL },
            timeoutMs = config.timeoutMs,
            maxRetries = config.maxRetries
        )
        return GeminiProvider(geminiConfig = geminiConfig)
    }

    fun createDefault(apiKey: String): GeminiProvider {
        val geminiConfig = GeminiConfiguration(apiKey = apiKey)
        return GeminiProvider(geminiConfig = geminiConfig)
    }
}
