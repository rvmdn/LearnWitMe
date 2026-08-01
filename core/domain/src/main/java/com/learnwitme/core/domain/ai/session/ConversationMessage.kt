package com.learnwitme.core.domain.ai.session

import kotlinx.serialization.Serializable

/**
 * Immutable value object representing a single message in an AI conversation session.
 */
@Serializable
data class ConversationMessage(
    val id: String,
    val role: ConversationRole,
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = 0L
) {
    init {
        require(id.isNotBlank()) { "Message id cannot be blank" }
    }

    val isSystemMessage: Boolean get() = role == ConversationRole.SYSTEM
    val isUserMessage: Boolean get() = role == ConversationRole.USER
    val isAssistantMessage: Boolean get() = role == ConversationRole.ASSISTANT
    val isToolMessage: Boolean get() = role == ConversationRole.TOOL
}
