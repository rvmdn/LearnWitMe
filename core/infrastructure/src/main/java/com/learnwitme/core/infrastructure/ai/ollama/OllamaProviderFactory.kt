package com.learnwitme.core.infrastructure.ai.ollama

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AIProviderFactory

/**
 * Factory for creating configured [OllamaProvider] instances.
 */
class OllamaProviderFactory : AIProviderFactory<OllamaProvider> {

    override fun createProvider(config: AIProviderConfiguration): OllamaProvider {
        val ollamaConfig = OllamaConfiguration(
            model = config.defaultModel.ifBlank { OllamaConfiguration.DEFAULT_MODEL },
            baseUrl = config.baseUrl.ifBlank { OllamaConfiguration.DEFAULT_BASE_URL },
            timeoutMs = config.timeoutMs,
            maxRetries = config.maxRetries
        )
        return OllamaProvider(config = ollamaConfig)
    }
}
