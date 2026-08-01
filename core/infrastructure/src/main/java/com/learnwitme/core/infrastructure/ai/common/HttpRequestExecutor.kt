package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto

/**
 * Low-level HTTP transport request executor interface.
 */
interface HttpRequestExecutor {
    fun executeHttp(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto,
        headers: Map<String, String>,
        requestBodyJson: String
    ): TransportResult
}
