package com.learnwitme.core.infrastructure.ai.common

import kotlinx.serialization.Serializable

@Serializable
enum class ProviderSelectionMode {
    AUTO,
    GEMINI,
    OPENAI,
    OPENROUTER,
    OLLAMA
}

/**
 * Immutable policy dictating provider priority and selection behavior.
 */
@Serializable
data class ProviderPriorityPolicy(
    val selectionMode: ProviderSelectionMode = ProviderSelectionMode.AUTO,
    val preferredOrder: List<String> = listOf("GEMINI", "OPENAI", "OPENROUTER", "OLLAMA"),
    val allowFallback: Boolean = true
)

/**
 * Sensible defaults for provider selection policies.
 */
object DefaultProviderPolicy {
    val DEFAULT = ProviderPriorityPolicy()
    val OFFLINE_PREFERRED = ProviderPriorityPolicy(
        selectionMode = ProviderSelectionMode.AUTO,
        preferredOrder = listOf("OLLAMA", "GEMINI", "OPENAI", "OPENROUTER")
    )
}
