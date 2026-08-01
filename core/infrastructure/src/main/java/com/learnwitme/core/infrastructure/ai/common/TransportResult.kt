package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.infrastructure.ai.AITransportResponseDto

/**
 * Sealed hierarchy representing the result of an AI transport HTTP execution.
 */
sealed class TransportResult {
    data class Success(val responseDto: AITransportResponseDto) : TransportResult()
    data class Failure(
        val statusCode: Int,
        val message: String,
        val rawResponseBody: String? = null,
        val cause: Throwable? = null
    ) : TransportResult()
}
