package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import kotlinx.serialization.Serializable

/**
 * Infrastructure configuration for Google Gemini AI REST provider.
 */
@Serializable
data class GeminiConfiguration(
    val apiKey: String = "",
    val model: String = DEFAULT_MODEL,
    val baseUrl: String = DEFAULT_BASE_URL,
    val timeoutMs: Long = 30_000L,
    val maxRetries: Int = 3
) {
    fun toProviderConfiguration(): AIProviderConfiguration {
        return AIProviderConfiguration(
            providerName = "GEMINI",
            baseUrl = baseUrl,
            apiKey = apiKey,
            defaultModel = model,
            timeoutMs = timeoutMs,
            maxRetries = maxRetries
        )
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-1.5-flash"
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    }
}
