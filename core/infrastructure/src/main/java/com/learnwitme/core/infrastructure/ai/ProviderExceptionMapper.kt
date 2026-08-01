package com.learnwitme.core.infrastructure.ai

import com.learnwitme.core.domain.ai.AIProviderException

/**
 * Infrastructure mapper translating transport-level exceptions and HTTP error status codes
 * into domain-level [AIProviderException] instances.
 */
class ProviderExceptionMapper {

    fun mapToDomainException(
        statusCode: Int,
        message: String,
        provider: String = "UNKNOWN",
        cause: Throwable? = null
    ): AIProviderException {
        return when (statusCode) {
            401, 403 -> AIProviderException.InvalidApiKeyException(
                message = "[$provider] Invalid or unauthorized API Key ($statusCode): $message",
                cause = cause
            )
            404 -> AIProviderException.ModelNotFoundException(
                message = "[$provider] Requested resource or model not found ($statusCode): $message",
                cause = cause
            )
            429 -> AIProviderException.RateLimitException(
                message = "[$provider] Rate limit exceeded ($statusCode): $message",
                cause = cause
            )
            408, 504 -> AIProviderException.NetworkTimeoutException(
                message = "[$provider] Transport request timed out ($statusCode): $message",
                cause = cause
            )
            else -> AIProviderException.GenericAIException(
                message = "[$provider] Transport error ($statusCode): $message",
                cause = cause
            )
        }
    }

    fun mapThrowableToDomainException(
        throwable: Throwable,
        provider: String = "UNKNOWN"
    ): AIProviderException {
        if (throwable is AIProviderException) return throwable

        val msg = throwable.message ?: "Unknown error"
        return when {
            msg.contains("timeout", ignoreCase = true) -> AIProviderException.NetworkTimeoutException(
                message = "[$provider] Network timeout: $msg",
                cause = throwable
            )
            msg.contains("rate limit", ignoreCase = true) -> AIProviderException.RateLimitException(
                message = "[$provider] Rate limit hit: $msg",
                cause = throwable
            )
            else -> AIProviderException.GenericAIException(
                message = "[$provider] Transport failure: $msg",
                cause = throwable
            )
        }
    }
}
