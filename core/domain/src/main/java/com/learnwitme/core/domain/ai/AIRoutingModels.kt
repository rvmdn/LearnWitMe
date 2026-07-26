package com.learnwitme.core.domain.ai

/**
 * Policy governing AI provider selection and fallback strategies.
 */
data class AIProviderSelectionPolicy(
    val preferredProviderType: AIProviderType = AIProviderType.MOCK,
    val fallbackProviderType: AIProviderType? = null,
    val allowFallback: Boolean = true
)
