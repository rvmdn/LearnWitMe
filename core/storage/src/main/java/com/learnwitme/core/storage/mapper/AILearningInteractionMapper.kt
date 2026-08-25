package com.learnwitme.core.storage.mapper

import com.learnwitme.core.application.ai.feedback.AIInteractionType
import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningImpactClassification
import com.learnwitme.core.application.ai.feedback.AILearningInteraction
import com.learnwitme.core.application.ai.feedback.AILearningMemoryImpact
import com.learnwitme.core.application.ai.feedback.AILearningOutcome
import com.learnwitme.core.application.ai.feedback.AIOutcomeStatus
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.storage.local.entity.AILearningFeedbackEntity
import com.learnwitme.core.storage.local.entity.AILearningInteractionEntity
import com.learnwitme.core.storage.local.entity.AILearningMemoryImpactEntity
import com.learnwitme.core.storage.local.entity.AILearningOutcomeEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Pure, deterministic mappings between AI learning artifacts and Room entities. */
object AILearningInteractionMapper {
    private val json = Json { encodeDefaults = true }

    fun toEntity(interaction: AILearningInteraction): AILearningInteractionEntity =
        AILearningInteractionEntity(
            interactionId = interaction.interactionId,
            userId = interaction.userId.value,
            action = interaction.action.name,
            cardId = interaction.cardId?.value,
            topicCode = interaction.topic?.code,
            topicName = interaction.topic?.name,
            topicDescription = interaction.topic?.description,
            startedAt = interaction.startedAt,
            completedAt = interaction.completedAt,
            provider = interaction.provider?.name,
            model = interaction.model,
            promptTokens = interaction.promptTokens,
            completionTokens = interaction.completionTokens,
            personalizationPreset = interaction.personalizationPreset
        )

    fun toDomain(entity: AILearningInteractionEntity): AILearningInteraction =
        AILearningInteraction(
            interactionId = entity.interactionId,
            userId = UserId(entity.userId),
            action = AIInteractionType.valueOf(entity.action),
            cardId = entity.cardId?.let(::KnowledgeCardId),
            topic = entity.toTopicOrNull(),
            startedAt = entity.startedAt,
            completedAt = entity.completedAt,
            provider = entity.provider?.let(AIProviderType::valueOf),
            model = entity.model,
            promptTokens = entity.promptTokens,
            completionTokens = entity.completionTokens,
            personalizationPreset = entity.personalizationPreset
        )

    fun toEntity(feedback: AILearningFeedback): AILearningFeedbackEntity =
        AILearningFeedbackEntity(
            interactionId = feedback.interactionId,
            usefulnessRating = feedback.usefulnessRating,
            confidenceSignal = feedback.confidenceSignal,
            comment = feedback.comment,
            createdAt = feedback.createdAt
        )

    fun toDomain(entity: AILearningFeedbackEntity): AILearningFeedback =
        AILearningFeedback(
            interactionId = entity.interactionId,
            usefulnessRating = entity.usefulnessRating,
            confidenceSignal = entity.confidenceSignal,
            comment = entity.comment,
            createdAt = entity.createdAt
        )

    fun toEntity(outcome: AILearningOutcome): AILearningOutcomeEntity =
        AILearningOutcomeEntity(
            interactionId = outcome.interactionId,
            status = outcome.status.name,
            summary = outcome.summary,
            completedAt = outcome.completedAt,
            metadataJson = json.encodeToString(outcome.metadata)
        )

    fun toDomain(entity: AILearningOutcomeEntity): AILearningOutcome =
        AILearningOutcome(
            interactionId = entity.interactionId,
            status = AIOutcomeStatus.valueOf(entity.status),
            summary = entity.summary,
            completedAt = entity.completedAt,
            metadata = json.decodeFromString(entity.metadataJson)
        )

    fun toEntity(impact: AILearningMemoryImpact): AILearningMemoryImpactEntity =
        AILearningMemoryImpactEntity(
            interactionId = impact.interactionId,
            cardId = impact.cardId?.value,
            topicCode = impact.topic?.code,
            topicName = impact.topic?.name,
            topicDescription = impact.topic?.description,
            action = impact.action.name,
            outcomeStatus = impact.outcomeStatus?.name,
            usefulnessRating = impact.usefulnessRating,
            confidenceSignal = impact.confidenceSignal,
            classification = impact.classification.name,
            confidenceDelta = impact.confidenceDelta,
            calculatedAt = impact.calculatedAt
        )

    fun toDomain(entity: AILearningMemoryImpactEntity): AILearningMemoryImpact =
        AILearningMemoryImpact(
            interactionId = entity.interactionId,
            cardId = entity.cardId?.let(::KnowledgeCardId),
            topic = entity.toTopicOrNull(),
            action = AIInteractionType.valueOf(entity.action),
            outcomeStatus = entity.outcomeStatus?.let(AIOutcomeStatus::valueOf),
            usefulnessRating = entity.usefulnessRating,
            confidenceSignal = entity.confidenceSignal,
            classification = AILearningImpactClassification.valueOf(entity.classification),
            confidenceDelta = entity.confidenceDelta,
            calculatedAt = entity.calculatedAt
        )

    private fun AILearningInteractionEntity.toTopicOrNull(): Topic? =
        topicCode?.let { code -> Topic(code, requireNotNull(topicName), requireNotNull(topicDescription)) }

    private fun AILearningMemoryImpactEntity.toTopicOrNull(): Topic? =
        topicCode?.let { code -> Topic(code, requireNotNull(topicName), requireNotNull(topicDescription)) }
}
