package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AILearningFeedbackTest {

    private val sampleUserId = UserId("user_123")
    private val sampleCardId = KnowledgeCardId("card_456")
    private val sampleTopic = Topic("tp_kotlin", "Kotlin Basics", "Basic Kotlin Programming Concepts")

    // --- 1. Immutable Construction & Defaults ---

    @Test
    fun testValidAILearningInteractionConstruction() {
        val interaction = AILearningInteraction(
            interactionId = "int_001",
            userId = sampleUserId,
            action = AIInteractionType.LESSON,
            cardId = sampleCardId,
            topic = sampleTopic,
            startedAt = 1000L,
            completedAt = 1500L,
            provider = AIProviderType.GEMINI,
            model = "gemini-1.5-flash",
            promptTokens = 100,
            completionTokens = 50,
            personalizationPreset = "FULL"
        )

        assertEquals("int_001", interaction.interactionId)
        assertEquals(sampleUserId, interaction.userId)
        assertEquals(AIInteractionType.LESSON, interaction.action)
        assertEquals(sampleCardId, interaction.cardId)
        assertEquals(sampleTopic, interaction.topic)
        assertEquals(1000L, interaction.startedAt)
        assertEquals(1500L, interaction.completedAt)
        assertEquals(AIProviderType.GEMINI, interaction.provider)
        assertEquals("gemini-1.5-flash", interaction.model)
        assertEquals(100, interaction.promptTokens)
        assertEquals(50, interaction.completionTokens)
        assertEquals(150, interaction.totalTokens)
        assertEquals("FULL", interaction.personalizationPreset)
    }

    @Test
    fun testValidAILearningOutcomeConstruction() {
        val outcome = AILearningOutcome(
            interactionId = "int_001",
            status = AIOutcomeStatus.COMPLETED,
            summary = "Learner mastered lesson content successfully",
            completedAt = 1500L,
            metadata = mapOf("score" to "0.95")
        )

        assertEquals("int_001", outcome.interactionId)
        assertEquals(AIOutcomeStatus.COMPLETED, outcome.status)
        assertEquals("Learner mastered lesson content successfully", outcome.summary)
        assertEquals(1500L, outcome.completedAt)
        assertEquals("0.95", outcome.metadata["score"])
    }

    @Test
    fun testValidAILearningFeedbackConstruction() {
        val feedback = AILearningFeedback(
            interactionId = "int_001",
            usefulnessRating = 5,
            confidenceSignal = 0.90,
            comment = "Very clear explanation!",
            createdAt = 1600L
        )

        assertEquals("int_001", feedback.interactionId)
        assertEquals(5, feedback.usefulnessRating)
        assertEquals(0.90, feedback.confidenceSignal!!, 0.001)
        assertEquals("Very clear explanation!", feedback.comment)
        assertEquals(1600L, feedback.createdAt)
    }

    // --- 2. Invariant Validation ---

    @Test(expected = IllegalArgumentException::class)
    fun testInteractionBlankIdThrowsException() {
        AILearningInteraction(
            interactionId = "   ",
            userId = sampleUserId,
            action = AIInteractionType.EXPLANATION,
            startedAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInteractionInvalidStartedAtThrowsException() {
        AILearningInteraction(
            interactionId = "int_001",
            userId = sampleUserId,
            action = AIInteractionType.QUIZ,
            startedAt = 0L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInteractionCompletedAtBeforeStartedAtThrowsException() {
        AILearningInteraction(
            interactionId = "int_001",
            userId = sampleUserId,
            action = AIInteractionType.SIMPLIFICATION,
            startedAt = 2000L,
            completedAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInteractionNegativePromptTokensThrowsException() {
        AILearningInteraction(
            interactionId = "int_001",
            userId = sampleUserId,
            action = AIInteractionType.LESSON,
            startedAt = 1000L,
            promptTokens = -10
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOutcomeBlankInteractionIdThrowsException() {
        AILearningOutcome(
            interactionId = "",
            status = AIOutcomeStatus.FAILED,
            completedAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOutcomeInvalidCompletedAtThrowsException() {
        AILearningOutcome(
            interactionId = "int_001",
            status = AIOutcomeStatus.REJECTED,
            completedAt = -1L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFeedbackRatingUnderflowThrowsException() {
        AILearningFeedback(
            interactionId = "int_001",
            usefulnessRating = 0,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFeedbackRatingOverflowThrowsException() {
        AILearningFeedback(
            interactionId = "int_001",
            usefulnessRating = 6,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFeedbackInvalidConfidenceSignalThrowsException() {
        AILearningFeedback(
            interactionId = "int_001",
            usefulnessRating = 4,
            confidenceSignal = 1.5,
            createdAt = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFeedbackInvalidCreatedAtThrowsException() {
        AILearningFeedback(
            interactionId = "int_001",
            usefulnessRating = 3,
            createdAt = 0L
        )
    }

    // --- 3. All Enum States Coverage ---

    @Test
    fun testAllAIInteractionTypeEnumValues() {
        val expected = setOf(
            AIInteractionType.LESSON,
            AIInteractionType.QUIZ,
            AIInteractionType.EXPLANATION,
            AIInteractionType.KNOWLEDGE_EXPANSION,
            AIInteractionType.SIMPLIFICATION
        )
        assertEquals(expected, AIInteractionType.values().toSet())
    }

    @Test
    fun testAllAIOutcomeStatusEnumValues() {
        val expected = setOf(
            AIOutcomeStatus.COMPLETED,
            AIOutcomeStatus.PARTIALLY_COMPLETED,
            AIOutcomeStatus.REJECTED,
            AIOutcomeStatus.FAILED,
            AIOutcomeStatus.SKIPPED
        )
        assertEquals(expected, AIOutcomeStatus.values().toSet())
    }

    // --- 4. Deterministic Equality & Immutability ---

    @Test
    fun testDeterministicEqualityAndCopy() {
        val original = AILearningInteraction(
            interactionId = "int_100",
            userId = sampleUserId,
            action = AIInteractionType.KNOWLEDGE_EXPANSION,
            startedAt = 1000L
        )

        val identical = AILearningInteraction(
            interactionId = "int_100",
            userId = sampleUserId,
            action = AIInteractionType.KNOWLEDGE_EXPANSION,
            startedAt = 1000L
        )

        val updated = original.copy(
            completedAt = 2000L,
            promptTokens = 200,
            completionTokens = 100
        )

        assertEquals(original, identical)
        assertEquals(original.hashCode(), identical.hashCode())
        assertNotEquals(original, updated)
        assertEquals(300, updated.totalTokens)
    }

    // --- 5. Interaction and Outcome Relationship ---

    @Test
    fun testInteractionOutcomeRelationshipBinding() {
        val startTime = 1000L
        val completionTime = 2500L

        val interaction = AILearningInteraction(
            interactionId = "int_rel_01",
            userId = sampleUserId,
            action = AIInteractionType.LESSON,
            startedAt = startTime,
            completedAt = completionTime
        )

        val outcome = AILearningOutcome(
            interactionId = interaction.interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Lesson completed successfully",
            completedAt = completionTime
        )

        assertEquals(interaction.interactionId, outcome.interactionId)
        assertEquals(interaction.completedAt, outcome.completedAt)
        assertTrue(outcome.completedAt >= interaction.startedAt)
    }
}
