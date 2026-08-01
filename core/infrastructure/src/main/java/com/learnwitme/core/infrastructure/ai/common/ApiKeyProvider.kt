package com.learnwitme.core.infrastructure.ai.common

/**
 * Infrastructure interface for obtaining API keys for AI provider requests.
 */
fun interface ApiKeyProvider {
    fun getApiKey(providerId: String): String
}
