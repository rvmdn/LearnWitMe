package com.learnwitme.core.infrastructure.ai.common

/**
 * Multi-provider selection coordinator choosing providers by policy and capability.
 */
class ProviderSelector(
    private val resolver: ProviderCapabilityResolver = ProviderCapabilityResolver(),
    private val fallbackChain: ProviderFallbackChain = ProviderFallbackChain(resolver)
) {

    fun selectProviderName(
        registeredProviders: Map<String, ProviderMetadata>,
        policy: ProviderPriorityPolicy = DefaultProviderPolicy.DEFAULT,
        requiredCapability: ProviderCapability = ProviderCapability()
    ): String {
        return when (policy.selectionMode) {
            ProviderSelectionMode.AUTO -> {
                policy.preferredOrder.firstOrNull { name ->
                    val meta = registeredProviders[name]
                    meta != null && resolver.satisfiesCapabilities(meta.capabilities, requiredCapability)
                } ?: registeredProviders.keys.firstOrNull()
                ?: throw IllegalStateException("No registered providers found")
            }
            else -> {
                val target = policy.selectionMode.name
                if (registeredProviders.containsKey(target)) {
                    target
                } else if (policy.allowFallback) {
                    policy.preferredOrder.firstOrNull { registeredProviders.containsKey(it) }
                        ?: throw IllegalStateException("Selected provider $target and fallbacks unavailable")
                } else {
                    throw IllegalStateException("Selected provider $target unavailable")
                }
            }
        }
    }

    fun <T> executeWithSelection(
        registeredProviders: Map<String, Pair<ProviderMetadata, () -> T>>,
        policy: ProviderPriorityPolicy = DefaultProviderPolicy.DEFAULT,
        requiredCapability: ProviderCapability = ProviderCapability()
    ): T {
        val selectedOrder = when (policy.selectionMode) {
            ProviderSelectionMode.AUTO -> policy.preferredOrder
            else -> {
                val chosen = policy.selectionMode.name
                if (policy.allowFallback) {
                    listOf(chosen) + (policy.preferredOrder - chosen)
                } else {
                    listOf(chosen)
                }
            }
        }

        val orderedProviders = selectedOrder.mapNotNull { name ->
            registeredProviders[name]
        }

        return fallbackChain.executeWithFallback(
            providers = orderedProviders,
            requiredCapability = requiredCapability,
            allowFallback = policy.allowFallback
        )
    }
}
