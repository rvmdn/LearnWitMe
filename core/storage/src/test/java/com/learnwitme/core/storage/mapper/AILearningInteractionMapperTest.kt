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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AILearningInteractionMapperTest {
    private val topic = Topic("tp_ai", "Artificial Intelligence", "AI learning topic")

    @Test
    fun interactionMappingIsDeterministicAndPreservesNullableFields() {
        val interaction = AILearningInteraction(
            interactionId = "interaction-1",
            userId = UserId("user-1"),
            action = AIInteractionType.LESSON,
            cardId = KnowledgeCardId("card-1"),
            topic = topic,
            startedAt = 1_000L,
            completedAt = null,
            provider = AIProviderType.OPENAI,
            model = "gpt-test",
            promptTokens = 12,
            completionTokens = 34,
            personalizationPreset = null
        )

        val entity = AILearningInteractionMapper.toEntity(interaction)

        assertEquals(entity, AILearningInteractionMapper.toEntity(interaction))
        assertNull(entity.completedAt)
        assertNull(entity.personalizationPreset)
        assertEquals(interaction, AILearningInteractionMapper.toDomain(entity))
    }

    @Test
    fun feedbackOutcomeAndImpactMappingsRoundTrip() {
        val feedback = AILearningFeedback("interaction-1", 5, null, null, 2_000L)
        val outcome = AILearningOutcome(
            interactionId = "interaction-1",
            status = AIOutcomeStatus.COMPLETED,
            summary = "Completed lesson",
            completedAt = 2_100L,
            metadata = mapOf("source" to "test")
        )
        val impact = AILearningMemoryImpact(
            interactionId = "interaction-1",
            cardId = null,
            topic = topic,
            action = AIInteractionType.LESSON,
            outcomeStatus = AIOutcomeStatus.COMPLETED,
            usefulnessRating = 5,
            confidenceSignal = 0.8,
            classification = AILearningImpactClassification.POSITIVE_REINFORCEMENT,
            confidenceDelta = 0.2,
            calculatedAt = 2_200L
        )

        assertEquals(feedback, AILearningInteractionMapper.toDomain(AILearningInteractionMapper.toEntity(feedback)))
        assertEquals(outcome, AILearningInteractionMapper.toDomain(AILearningInteractionMapper.toEntity(outcome)))
        assertEquals(impact, AILearningInteractionMapper.toDomain(AILearningInteractionMapper.toEntity(impact)))
    }
}
