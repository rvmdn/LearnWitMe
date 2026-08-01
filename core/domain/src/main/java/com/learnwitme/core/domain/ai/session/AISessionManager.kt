package com.learnwitme.core.domain.ai.session

import com.learnwitme.core.domain.ai.prompt.PromptBuildResult
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy

/**
 * Pure coordinator for managing AI conversation sessions and context windows deterministically.
 */
class AISessionManager(
    private val contextAssembler: ContextAssembler = ContextAssembler(),
    private val compressor: ConversationCompressor = ConversationCompressor()
) {

    fun startSession(
        sessionId: String,
        conversationId: String,
        userId: String = "",
        timestamp: Long = 0L
    ): AISession {
        return AISession(
            sessionId = sessionId,
            conversationId = conversationId,
            userId = userId,
            history = ConversationHistory.EMPTY,
            currentContextWindow = ContextWindow.EMPTY,
            createdAt = timestamp,
            updatedAt = timestamp
        )
    }

    fun appendMessage(
        session: AISession,
        message: ConversationMessage,
        timestamp: Long = 0L
    ): AISession {
        val updatedHistory = session.history.append(message)
        val updatedContext = contextAssembler.assemble(
            history = updatedHistory,
            maxTokens = session.currentContextWindow.maxTokens
        )
        return session.copy(
            history = updatedHistory,
            currentContextWindow = updatedContext,
            updatedAt = timestamp
        )
    }

    fun rebuildContext(
        session: AISession,
        promptBuildResult: PromptBuildResult? = null,
        executionPolicy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
        maxTokens: Int = 2048
    ): AISession {
        val newContext = contextAssembler.assemble(
            history = session.history,
            promptBuildResult = promptBuildResult,
            executionPolicy = executionPolicy,
            maxTokens = maxTokens
        )
        return session.copy(
            currentContextWindow = newContext
        )
    }

    fun compressIfNecessary(
        session: AISession,
        maxTokens: Int = 2048
    ): AISession {
        val compressedMessages = compressor.compress(
            messages = session.history.messages,
            maxTokenLimit = maxTokens
        )
        val newHistory = ConversationHistory(messages = compressedMessages)
        val newContext = contextAssembler.assemble(
            history = newHistory,
            maxTokens = maxTokens
        )
        return session.copy(
            history = newHistory,
            currentContextWindow = newContext
        )
    }

    fun closeSession(
        session: AISession,
        timestamp: Long = 0L
    ): AISession {
        return session.copy(
            updatedAt = timestamp
        )
    }
}
