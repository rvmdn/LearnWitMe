package com.learnwitme.core.domain.ai

/**
 * Responsibility: Pure domain router responsible for resolving preferred or fallback AIProvider
 * implementations from AIProviderRegistry according to AIProviderSelectionPolicy.
 */
class AIProviderRouter(
    private val registry: AIProviderRegistry
) {
    fun resolveProvider(policy: AIProviderSelectionPolicy): AIProvider {
        val preferred = registry.getProvider(policy.preferredProviderType)
        if (preferred != null) {
            return preferred
        }

        if (policy.allowFallback && policy.fallbackProviderType != null) {
            val fallback = registry.getProvider(policy.fallbackProviderType)
            if (fallback != null) {
                return fallback
            }
        }

        // Ultimate safety fallback: MOCK provider if registered
        if (policy.allowFallback && registry.hasProvider(AIProviderType.MOCK)) {
            val mockProvider = registry.getProvider(AIProviderType.MOCK)
            if (mockProvider != null) {
                return mockProvider
            }
        }

        throw AIProviderException.ModelNotFoundException(
            "No active AIProvider registered for type '${policy.preferredProviderType.name}' and fallback resolution failed."
        )
    }
}
