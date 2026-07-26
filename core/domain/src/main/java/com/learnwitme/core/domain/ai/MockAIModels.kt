package com.learnwitme.core.domain.ai

/**
 * Configuration governing MockAIProvider behavior, simulated latency, and error injection.
 */
data class MockAIConfiguration(
    val shouldSimulateFailure: Boolean = false,
    val failureException: AIProviderException? = null,
    val simulatedExecutionTimeMs: Long = 50L,
    val modelId: String = "mock-ai-v1"
)
