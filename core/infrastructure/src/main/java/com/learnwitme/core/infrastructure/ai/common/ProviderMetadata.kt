package com.learnwitme.core.infrastructure.ai.common

import kotlinx.serialization.Serializable

/**
 * Enhanced capability descriptor for AI providers.
 */
@Serializable
data class ProviderCapability(
    val supportsTextGeneration: Boolean = true,
    val supportsSystemInstruction: Boolean = true,
    val supportsJson: Boolean = true,
    val supportsQuiz: Boolean = true,
    val supportsLesson: Boolean = true,
    val supportsStreaming: Boolean = false,
    val supportsVision: Boolean = false,
    val supportsOffline: Boolean = false,
    val supportsReasoning: Boolean = false
)

/**
 * Metadata descriptor for an AI provider.
 */
@Serializable
data class ProviderMetadata(
    val providerId: String,
    val providerName: String,
    val defaultModel: String,
    val capabilities: ProviderCapability = ProviderCapability()
)
