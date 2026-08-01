package com.learnwitme.core.infrastructure.ai

import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest

/**
 * Infrastructure contract for mapping domain-level [AIExecutionRequest] objects to transport DTOs.
 */
interface AIRequestMapper<out T> {

    /**
     * Maps a domain execution request into a provider-specific or common transport DTO payload.
     */
    fun mapToTransportRequest(
        request: AIExecutionRequest,
        config: AIProviderConfiguration
    ): T
}
