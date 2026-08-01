package com.learnwitme.core.domain.ai.session

import kotlinx.serialization.Serializable

/**
 * Supported conversation message participant roles.
 */
@Serializable
enum class ConversationRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}
