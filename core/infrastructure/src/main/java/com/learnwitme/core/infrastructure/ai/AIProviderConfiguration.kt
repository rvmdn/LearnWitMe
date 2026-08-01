package com.learnwitme.core.infrastructure.ai

import kotlinx.serialization.Serializable

/**
 * Infrastructure configuration for establishing transport connections to an AI provider.
 */
@Serializable
data class AIProviderConfiguration(
    val providerName: String,
    val baseUrl: String,
    val apiKey: String = "",
    val defaultModel: String = "",
    val timeoutMs: Long = 30_000L,
    val maxRetries: Int = 3,
    val additionalHeaders: Map<String, String> = emptyMap()
) {
    init {
        require(providerName.isNotBlank()) { "providerName cannot be blank" }
        require(baseUrl.isNotBlank()) { "baseUrl cannot be blank" }
        require(timeoutMs > 0) { "timeoutMs must be positive" }
    }
}
