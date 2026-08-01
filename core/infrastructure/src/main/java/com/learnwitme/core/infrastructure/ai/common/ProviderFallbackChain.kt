package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.domain.ai.AIProviderException

/**
 * Evaluates provider availability and executes fallback chains deterministically.
 */
class ProviderFallbackChain(
    private val capabilityResolver: ProviderCapabilityResolver = ProviderCapabilityResolver()
) {

    fun <T> executeWithFallback(
        providers: List<Pair<ProviderMetadata, () -> T>>,
        requiredCapability: ProviderCapability = ProviderCapability(),
        allowFallback: Boolean = true
    ): T {
        val eligibleProviders = providers.filter { (metadata, _) ->
            capabilityResolver.satisfiesCapabilities(metadata.capabilities, requiredCapability)
        }

        if (eligibleProviders.isEmpty()) {
            throw AIProviderException.ModelNotFoundException("No AI provider available satisfying capability requirements")
        }

        val providersToTry = if (allowFallback) eligibleProviders else eligibleProviders.take(1)
        var lastException: Throwable? = null

        for ((_, providerAction) in providersToTry) {
            try {
                return providerAction()
            } catch (t: Throwable) {
                lastException = t
                if (!allowFallback) throw t
            }
        }

        throw lastException ?: AIProviderException.GenericAIException("All fallback providers failed")
    }
}
