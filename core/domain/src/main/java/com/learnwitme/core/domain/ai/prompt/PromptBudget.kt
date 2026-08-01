package com.learnwitme.core.domain.ai.prompt

import kotlinx.serialization.Serializable

/**
 * Pure token size and budget estimator. Approximation without external SDK dependencies.
 */
class PromptBudget {

    @Serializable
    data class BudgetEvaluation(
        val estimatedTokens: Int,
        val remainingBudget: Int,
        val exceedsLimit: Boolean,
        val warning: String? = null
    )

    /**
     * Estimates token count using standard domain heuristic (~4 characters per token).
     */
    fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        return (text.length + 3) / 4
    }

    fun estimatePromptTokens(systemPrompt: String, userPrompt: String): Int {
        return estimateTokens(systemPrompt) + estimateTokens(userPrompt)
    }

    fun evaluate(
        systemPrompt: String,
        userPrompt: String,
        maxTokenLimit: Int
    ): BudgetEvaluation {
        val totalEstimated = estimatePromptTokens(systemPrompt, userPrompt)
        val remaining = maxTokenLimit - totalEstimated
        val exceeds = totalEstimated > maxTokenLimit
        val warning = if (exceeds) {
            "Estimated prompt tokens ($totalEstimated) exceeds max token limit ($maxTokenLimit)"
        } else null

        return BudgetEvaluation(
            estimatedTokens = totalEstimated,
            remainingBudget = remaining,
            exceedsLimit = exceeds,
            warning = warning
        )
    }

    companion object {
        const val CHARS_PER_TOKEN_HEURISTIC = 4
    }
}
