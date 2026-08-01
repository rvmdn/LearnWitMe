package com.learnwitme.core.infrastructure.ai.openrouter

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIProviderFactory

/**
 * Factory for creating configured [OpenRouterProvider] instances.
 */
class OpenRouterProviderFactory : AIProviderFactory<OpenRouterProvider> {

    override fun createProvider(config: AIProviderConfiguration): OpenRouterProvider {
        val openRouterConfig = OpenRouterConfiguration(
            apiKey = config.apiKey,
            model = config.defaultModel.ifBlank { OpenRouterConfiguration.DEFAULT_MODEL },
            baseUrl = config.baseUrl.ifBlank { OpenRouterConfiguration.DEFAULT_BASE_URL },
            timeoutMs = config.timeoutMs,
            maxRetries = config.maxRetries
        )
        return OpenRouterProvider(config = openRouterConfig)
    }
}
