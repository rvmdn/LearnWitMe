package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.infrastructure.ai.AITokenUsageDto
import com.learnwitme.core.infrastructure.ai.AITransportResponseDto

/**
 * Shared parser component for building [AITransportResponseDto] instances.
 */
class HttpResponseParser {

    fun parseSuccess(
        requestId: String,
        provider: String,
        model: String,
        content: String,
        finishReason: String = "STOP",
        promptTokens: Int = 0,
        completionTokens: Int = 0,
        rawPayload: String? = null
    ): AITransportResponseDto {
        val usage = AITokenUsageDto(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens
        )
        return AITransportResponseDto(
            requestId = requestId,
            provider = provider,
            model = model,
            content = content,
            finishReason = finishReason,
            usage = usage,
            statusCode = 200,
            rawPayload = rawPayload
        )
    }
}
