package com.learnwitme.core.infrastructure.ai

import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse

/**
 * Infrastructure contract for mapping transport-level response payloads into domain-level [AIExecutionResponse] objects.
 */
interface AIResponseMapper<in T> {

    /**
     * Maps a raw transport payload or DTO into a normalized domain [AIExecutionResponse].
     */
    fun mapToDomainResponse(
        transportResponse: T,
        requestId: String
    ): AIExecutionResponse
}
