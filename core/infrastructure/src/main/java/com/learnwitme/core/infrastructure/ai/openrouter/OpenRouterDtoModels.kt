package com.learnwitme.core.infrastructure.ai.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<OpenRouterChatMessage>,
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null
)

@Serializable
data class OpenRouterChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenRouterChatChoice>? = null,
    val usage: OpenRouterUsageMetadata? = null
)

@Serializable
data class OpenRouterChatChoice(
    val index: Int = 0,
    val message: OpenRouterChatMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class OpenRouterUsageMetadata(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)
