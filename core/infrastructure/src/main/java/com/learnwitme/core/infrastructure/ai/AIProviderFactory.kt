package com.learnwitme.core.infrastructure.ai

/**
 * Infrastructure factory contract for creating AI provider client instances from configuration.
 */
interface AIProviderFactory<out T> {

    /**
     * Creates a provider client instance using the supplied [AIProviderConfiguration].
     */
    fun createProvider(config: AIProviderConfiguration): T
}
