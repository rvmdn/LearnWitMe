package com.learnwitme.core.application.ai.result

/**
 * Thrown when an AIResponse content payload cannot be mapped into an application result model.
 */
class AIResultParsingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
