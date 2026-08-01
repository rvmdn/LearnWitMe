package com.learnwitme.core.domain.ai.session

import com.learnwitme.core.domain.ai.prompt.PromptBudget
import com.learnwitme.core.domain.ai.prompt.PromptBuildResult
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy

/**
 * Pure deterministic assembler that builds a [ContextWindow] from conversation history and prompt build artifacts.
 */
class ContextAssembler(
    private val budgetEstimator: PromptBudget = PromptBudget(),
    private val compressor: ConversationCompressor = ConversationCompressor()
) {

    fun assemble(
        history: ConversationHistory,
        promptBuildResult: PromptBuildResult? = null,
        executionPolicy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
        maxTokens: Int = 2048
    ): ContextWindow {
        val baseMessages = mutableListOf<ConversationMessage>()

        if (promptBuildResult != null) {
            if (promptBuildResult.renderedSystemPrompt.isNotBlank()) {
                baseMessages.add(
                    ConversationMessage(
                        id = "prompt_system",
                        role = ConversationRole.SYSTEM,
                        content = promptBuildResult.renderedSystemPrompt
                    )
                )
            }
            if (promptBuildResult.renderedUserPrompt.isNotBlank()) {
                baseMessages.add(
                    ConversationMessage(
                        id = "prompt_user",
                        role = ConversationRole.USER,
                        content = promptBuildResult.renderedUserPrompt
                    )
                )
            }
        }

        baseMessages.addAll(history.messages)

        val totalEstimatedBeforeCompress = baseMessages.sumOf { budgetEstimator.estimateTokens(it.content) }
        val finalMessages = if (totalEstimatedBeforeCompress > maxTokens) {
            compressor.compress(baseMessages, maxTokens)
        } else {
            baseMessages
        }

        val finalEstimatedTokens = finalMessages.sumOf { budgetEstimator.estimateTokens(it.content) }
        val remaining = (maxTokens - finalEstimatedTokens).coerceAtLeast(0)

        return ContextWindow(
            messages = finalMessages,
            estimatedTokens = finalEstimatedTokens,
            maxTokens = maxTokens,
            remainingBudget = remaining
        )
    }
}
