package com.learnwitme.core.infrastructure.ai.common

/**
 * Pure resolver matching requested capabilities against provider capability descriptors.
 */
class ProviderCapabilityResolver {

    fun satisfiesCapabilities(
        providerCapability: ProviderCapability,
        requiredCapability: ProviderCapability
    ): Boolean {
        if (requiredCapability.supportsJson && !providerCapability.supportsJson) return false
        if (requiredCapability.supportsQuiz && !providerCapability.supportsQuiz) return false
        if (requiredCapability.supportsLesson && !providerCapability.supportsLesson) return false
        if (requiredCapability.supportsStreaming && !providerCapability.supportsStreaming) return false
        if (requiredCapability.supportsVision && !providerCapability.supportsVision) return false
        if (requiredCapability.supportsOffline && !providerCapability.supportsOffline) return false
        if (requiredCapability.supportsReasoning && !providerCapability.supportsReasoning) return false
        return true
    }
}
