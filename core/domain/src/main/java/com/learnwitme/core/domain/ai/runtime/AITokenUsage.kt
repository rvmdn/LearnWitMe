package com.learnwitme.core.domain.ai.runtime

import kotlinx.serialization.Serializable

/**
 * Immutable token accounting for an AI execution.
 */
@Serializable
data class AITokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = promptTokens + completionTokens
) {
    init {
        require(promptTokens >= 0) { "promptTokens cannot be negative" }
        require(completionTokens >= 0) { "completionTokens cannot be negative" }
        require(totalTokens >= 0) { "totalTokens cannot be negative" }
    }

    operator fun plus(other: AITokenUsage): AITokenUsage {
        return AITokenUsage(
            promptTokens = this.promptTokens + other.promptTokens,
            completionTokens = this.completionTokens + other.completionTokens,
            totalTokens = this.totalTokens + other.totalTokens
        )
    }

    companion object {
        val ZERO = AITokenUsage(promptTokens = 0, completionTokens = 0, totalTokens = 0)
    }
}
