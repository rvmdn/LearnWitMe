package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId

/**
 * Immutable model representing a single AI-assisted learning interaction.
 */
data class AILearningInteraction(
    val interactionId: String,
    val userId: UserId,
    val action: AIInteractionType,
    val cardId: KnowledgeCardId? = null,
    val topic: Topic? = null,
    val startedAt: Long,
    val completedAt: Long? = null,
    val provider: AIProviderType? = null,
    val model: String? = null,
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val personalizationPreset: String? = null
) {
    val totalTokens: Int get() = promptTokens + completionTokens

    init {
        require(interactionId.isNotBlank()) { "Interaction ID cannot be blank" }
        require(startedAt > 0) { "StartedAt timestamp must be positive" }
        completedAt?.let {
            require(it >= startedAt) { "CompletedAt timestamp ($it) cannot be earlier than startedAt ($startedAt)" }
        }
        model?.let {
            require(it.isNotBlank()) { "Model name cannot be blank if provided" }
        }
        personalizationPreset?.let {
            require(it.isNotBlank()) { "Personalization preset cannot be blank if provided" }
        }
        require(promptTokens >= 0) { "Prompt tokens cannot be negative" }
        require(completionTokens >= 0) { "Completion tokens cannot be negative" }
    }
}
