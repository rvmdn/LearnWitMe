package com.learnwitme.core.application.ai.personalization

import kotlinx.serialization.Serializable

/**
 * Immutable, provider-agnostic prompt payload containing structured instructions, user prompt, and metadata.
 */
@Serializable
data class PersonalizedPrompt(
    val systemPrompt: String,
    val userPrompt: String,
    val metadata: Map<String, String> = emptyMap(),
    val estimatedTokens: Int = 0
)
