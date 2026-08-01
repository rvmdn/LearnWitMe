package com.learnwitme.core.domain.ai.session

import kotlinx.serialization.Serializable

/**
 * Immutable active context window containing pruned/budgeted messages for an execution request.
 */
@Serializable
data class ContextWindow(
    val messages: List<ConversationMessage> = emptyList(),
    val estimatedTokens: Int = 0,
    val maxTokens: Int = 2048,
    val remainingBudget: Int = maxTokens - estimatedTokens
) {
    val isWithinBudget: Boolean get() = estimatedTokens <= maxTokens

    companion object {
        val EMPTY = ContextWindow()
    }
}
