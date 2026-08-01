package com.learnwitme.core.infrastructure.ai

/**
 * Infrastructure interface contract for executing low-level HTTP transport calls to AI providers.
 * Pure Kotlin contract; decoupled from specific HTTP engines like Retrofit or Ktor.
 */
interface AIHttpClient {

    /**
     * Executes a transport request synchronously.
     */
    fun execute(
        config: AIProviderConfiguration,
        requestDto: AITransportRequestDto
    ): AITransportResponseDto
}
