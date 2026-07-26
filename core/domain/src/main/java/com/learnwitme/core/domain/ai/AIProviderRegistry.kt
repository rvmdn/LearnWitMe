package com.learnwitme.core.domain.ai

/**
 * Responsibility: Instance-based registry for registering and looking up active AIProvider instances.
 * Note: Non-singleton instance to adhere to clean domain architecture rules.
 */
class AIProviderRegistry {
    private val providers = mutableMapOf<AIProviderType, AIProvider>()

    fun registerProvider(type: AIProviderType, provider: AIProvider) {
        providers[type] = provider
    }

    fun getProvider(type: AIProviderType): AIProvider? {
        return providers[type]
    }

    fun hasProvider(type: AIProviderType): Boolean {
        return providers.containsKey(type)
    }

    fun unregisterProvider(type: AIProviderType) {
        providers.remove(type)
    }

    fun clear() {
        providers.clear()
    }
}
