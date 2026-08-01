package com.learnwitme.core.domain.ai.session

import kotlinx.serialization.Serializable

/**
 * Immutable aggregate representation of an active AI conversation session.
 */
@Serializable
data class AISession(
    val sessionId: String,
    val conversationId: String,
    val userId: String = "",
    val history: ConversationHistory = ConversationHistory.EMPTY,
    val currentContextWindow: ContextWindow = ContextWindow.EMPTY,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    init {
        require(sessionId.isNotBlank()) { "sessionId cannot be blank" }
        require(conversationId.isNotBlank()) { "conversationId cannot be blank" }
    }
}
