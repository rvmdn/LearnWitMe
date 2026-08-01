package com.learnwitme.core.infrastructure.ai.openai

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIProviderFactory

/**
 * Factory for creating configured [OpenAIProvider] instances.
 */
class OpenAIProviderFactory : AIProviderFactory<OpenAIProvider> {

    override fun createProvider(config: AIProviderConfiguration): OpenAIProvider {
        val openAIConfig = OpenAIConfiguration(
            apiKey = config.apiKey,
            model = config.defaultModel.ifBlank { OpenAIConfiguration.DEFAULT_MODEL },
            baseUrl = config.baseUrl.ifBlank { OpenAIConfiguration.DEFAULT_BASE_URL },
            timeoutMs = config.timeoutMs,
            maxRetries = config.maxRetries
        )
        return OpenAIProvider(openAIConfig = openAIConfig)
    }

    fun createDefault(apiKey: String): OpenAIProvider {
        val openAIConfig = OpenAIConfiguration(apiKey = apiKey)
        return OpenAIProvider(openAIConfig = openAIConfig)
    }
}
