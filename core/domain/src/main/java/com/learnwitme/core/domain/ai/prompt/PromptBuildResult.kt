package com.learnwitme.core.domain.ai.prompt

import kotlinx.serialization.Serializable

/**
 * Immutable result returned by [PromptPipeline] after prompt construction, validation, sanitization, and token estimation.
 */
@Serializable
data class PromptBuildResult(
    val renderedSystemPrompt: String,
    val renderedUserPrompt: String,
    val estimatedTokenCount: Int,
    val warnings: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val isValid: Boolean = true
) {
    val totalCharacterCount: Int
        get() = renderedSystemPrompt.length + renderedUserPrompt.length
}
