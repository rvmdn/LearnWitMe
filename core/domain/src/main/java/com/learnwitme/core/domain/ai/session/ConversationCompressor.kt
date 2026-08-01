package com.learnwitme.core.domain.ai.session

import com.learnwitme.core.domain.ai.prompt.PromptBudget

/**
 * Pure deterministic compressor that prunes older non-system messages to fit context within token limits.
 * Operates without external AI calls or random heuristics.
 */
class ConversationCompressor(
    private val budgetEstimator: PromptBudget = PromptBudget()
) {

    fun compress(
        messages: List<ConversationMessage>,
        maxTokenLimit: Int
    ): List<ConversationMessage> {
        if (messages.isEmpty()) return emptyList()

        val systemMessages = messages.filter { it.role == ConversationRole.SYSTEM }
        val nonSystemMessages = messages.filterNot { it.role == ConversationRole.SYSTEM }

        val systemTokens = systemMessages.sumOf { budgetEstimator.estimateTokens(it.content) }
        val availableForNonSystem = (maxTokenLimit - systemTokens).coerceAtLeast(0)

        // Keep newest non-system messages from end to beginning
        val preservedNonSystem = mutableListOf<ConversationMessage>()
        var currentTokenSum = 0

        for (msg in nonSystemMessages.reversed()) {
            val msgTokens = budgetEstimator.estimateTokens(msg.content)
            if (currentTokenSum + msgTokens <= availableForNonSystem) {
                preservedNonSystem.add(0, msg)
                currentTokenSum += msgTokens
            } else {
                break
            }
        }

        return systemMessages + preservedNonSystem
    }
}
