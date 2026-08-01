package com.learnwitme.core.domain.ai.response

import kotlinx.serialization.Serializable

/**
 * Immutable metadata associated with a processed AI response.
 */
@Serializable
data class ResponseMetadata(
    val processingTimeMs: Long = 0L,
    val estimatedTokens: Int = 0,
    val containsMarkdown: Boolean = false,
    val containsJson: Boolean = false,
    val containsCodeBlock: Boolean = false,
    val language: String = "en",
    val normalizedCharacters: Int = 0
)
