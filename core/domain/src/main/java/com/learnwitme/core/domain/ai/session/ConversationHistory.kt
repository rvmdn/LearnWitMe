package com.learnwitme.core.domain.ai.session

import kotlinx.serialization.Serializable

/**
 * Immutable ordered history of conversation messages.
 * All mutations return new [ConversationHistory] instances.
 */
@Serializable
data class ConversationHistory(
    val messages: List<ConversationMessage> = emptyList()
) {
    val totalMessages: Int get() = messages.size

    val isEmpty: Boolean get() = messages.isEmpty()

    fun latest(): ConversationMessage? = messages.lastOrNull()

    fun append(message: ConversationMessage): ConversationHistory {
        return ConversationHistory(messages = this.messages + message)
    }

    fun remove(messageId: String): ConversationHistory {
        return ConversationHistory(messages = this.messages.filterNot { it.id == messageId })
    }

    fun clear(): ConversationHistory {
        return EMPTY
    }

    companion object {
        val EMPTY = ConversationHistory(emptyList())
    }
}
