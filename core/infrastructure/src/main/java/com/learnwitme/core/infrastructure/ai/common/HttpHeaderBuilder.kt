package com.learnwitme.core.infrastructure.ai.common

/**
 * Utility for building HTTP headers for AI transport requests.
 */
class HttpHeaderBuilder {

    fun buildHeaders(
        apiKey: String? = null,
        apiKeyHeaderName: String = "Authorization",
        apiKeyPrefix: String = "Bearer ",
        additionalHeaders: Map<String, String> = emptyMap()
    ): Map<String, String> {
        val headers = mutableMapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json"
        )

        if (!apiKey.isNullOrBlank()) {
            val headerValue = if (apiKeyPrefix.isNotBlank() && !apiKey.startsWith(apiKeyPrefix)) {
                "$apiKeyPrefix$apiKey"
            } else {
                apiKey
            }
            headers[apiKeyHeaderName] = headerValue
        }

        headers.putAll(additionalHeaders)
        return headers
    }
}
