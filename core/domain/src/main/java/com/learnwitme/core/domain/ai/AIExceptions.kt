package com.learnwitme.core.domain.ai

/**
 * Domain exception hierarchy representing AI provider failure states.
 */
sealed class AIProviderException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class RateLimitException(message: String = "AI Provider rate limit exceeded", cause: Throwable? = null) :
        AIProviderException(message, cause)

    class InvalidApiKeyException(message: String = "Invalid or missing AI API Key", cause: Throwable? = null) :
        AIProviderException(message, cause)

    class ModelNotFoundException(message: String = "Requested AI model not found", cause: Throwable? = null) :
        AIProviderException(message, cause)

    class NetworkTimeoutException(message: String = "AI Provider request timed out", cause: Throwable? = null) :
        AIProviderException(message, cause)

    class GenericAIException(message: String, cause: Throwable? = null) :
        AIProviderException(message, cause)
}
