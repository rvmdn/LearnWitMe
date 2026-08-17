package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AILearningMemoryFeedbackIntegratorTest {

    private val integrator = AILearningMemoryFeedbackIntegrator()
    private val calculator = AILearningMemoryImpactCalculator()
    private val sampleUserId = UserId("usr_test_123")
    private val otherUserId = UserId("usr_other_456")
    private val sampleCardId = KnowledgeCardId("card_k1")
    private val sampleTopic = Topic("tp_kotlin", "Kotlin Basics", "Basic Kotlin Programming Concepts")

    private fun createSampleInteraction(
        interactionId: String = "int_100",
        action: AIInteractionType = AIInteractionType.LESSON,
        cardId: KnowledgeCardId? = sampleCardId,
        topic: Topic? = sampleTopic,
        startedAt: Long = 1000L,
        completedAt: Long? = 1500L,
        userId: UserId = sampleUserId
    ): AILearningInteraction {
        return AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = action,
            cardId = cardId,
            topic = topic,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = AIProviderType.GEMINI,
            model = "gemini-1.5-pro",
            promptTokens = 120,
            completionTokens = 60,
            personalizationPreset = "FULL"
        )
    }

    private fun createSampleOutcome(
        interactionId: String = "int_100",
        status: AIOutcomeStatus = AIOutcomeStatus.COMPLETED,
        completedAt: Long = 1500L
    ): AILearningOutcome {
        return AILearningOutcome(
            interactionId = interactionId,
            status = status,
            summary = "Status is $status",
            completedAt = completedAt
        )
    }

    private fun createSampleMemory(
        cardConfidence: Double = 0.50,
        userId: UserId = sampleUserId
    ): LearningMemory {
        val record = MemoryRecord(
            cardId = sampleCardId,
            state = MemoryState.LEARNING,
            retention = 0.8,
            stability = 1.2,
            confidence = cardConfidence
        )
        return LearningMemory(
            id = LearningMemoryId("mem_001"),
            userId = userId,
            records = listOf(record)
        )
    }

    // 1. Action specific feedback tests (Lesson, Quiz, Explanation, Knowledge Expansion, Simplification)
    @Test
    fun testLessonFeedbackIntegration() {
        val interaction = createSampleInteraction(action = AIInteractionType.LESSON)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.85,
            comment = "Clear lesson",
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AIInteractionType.LESSON, success.impact.action)
        assertEquals(AILearningImpactClassification.CONFIDENCE_BOOST, success.impact.classification)
        assertEquals(0.10, success.impact.confidenceDelta, 0.001)
    }

    @Test
    fun testQuizFeedbackIntegration() {
        val interaction = createSampleInteraction(action = AIInteractionType.QUIZ)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.90,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AIInteractionType.QUIZ, success.impact.action)
        assertEquals(AILearningImpactClassification.CONFIDENCE_BOOST, success.impact.classification)
    }

    @Test
    fun testExplanationFeedbackIntegration() {
        val interaction = createSampleInteraction(action = AIInteractionType.EXPLANATION)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            confidenceSignal = 0.50,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AIInteractionType.EXPLANATION, success.impact.action)
        assertEquals(AILearningImpactClassification.POSITIVE_REINFORCEMENT, success.impact.classification)
        assertEquals(0.05, success.impact.confidenceDelta, 0.001)
    }

    @Test
    fun testKnowledgeExpansionFeedbackIntegration() {
        val interaction = createSampleInteraction(action = AIInteractionType.KNOWLEDGE_EXPANSION)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AIInteractionType.KNOWLEDGE_EXPANSION, success.impact.action)
        assertEquals(AILearningImpactClassification.POSITIVE_REINFORCEMENT, success.impact.classification)
    }

    @Test
    fun testSimplificationFeedbackIntegration() {
        val interaction = createSampleInteraction(action = AIInteractionType.SIMPLIFICATION)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            confidenceSignal = 0.75,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AIInteractionType.SIMPLIFICATION, success.impact.action)
        assertEquals(AILearningImpactClassification.CONFIDENCE_BOOST, success.impact.classification)
    }

    // 2. Useful feedback vs Low usefulness feedback
    @Test
    fun testLowUsefulnessRatingProducesRemediationSignal() {
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 1,
            comment = "Did not understand at all",
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AILearningImpactClassification.REMEDIATION_SIGNAL, success.impact.classification)
        assertEquals(-0.05, success.impact.confidenceDelta, 0.001)
    }

    // 3. Confidence signal handling
    @Test
    fun testLowConfidenceSignalWithHighUsefulnessProducesConfidenceReduction() {
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            confidenceSignal = 0.20, // useful content but learner feels unconfident
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AILearningImpactClassification.CONFIDENCE_REDUCTION, success.impact.classification)
        assertEquals(-0.05, success.impact.confidenceDelta, 0.001)
    }

    // 4. Outcome status handling (Completed, Skipped, Failed, Rejected)
    @Test
    fun testSkippedOutcomeProducesNeutralEngagement() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome(status = AIOutcomeStatus.SKIPPED)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 3,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback, outcome)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AILearningImpactClassification.NEUTRAL_ENGAGEMENT, success.impact.classification)
        assertEquals(0.0, success.impact.confidenceDelta, 0.001)
    }

    @Test
    fun testFailedOutcomeProducesRemediationSignal() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome(status = AIOutcomeStatus.FAILED)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 2,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback, outcome)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AILearningImpactClassification.REMEDIATION_SIGNAL, success.impact.classification)
        assertEquals(-0.05, success.impact.confidenceDelta, 0.001)
    }

    @Test
    fun testRejectedOutcomeProducesConfidenceReduction() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome(status = AIOutcomeStatus.REJECTED)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 3,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback, outcome)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals(AILearningImpactClassification.CONFIDENCE_REDUCTION, success.impact.classification)
        assertEquals(-0.05, success.impact.confidenceDelta, 0.001)
    }

    // 5. Missing card / topic handling
    @Test
    fun testIntegrationWithMissingCardAndTopic() {
        val interaction = createSampleInteraction(cardId = null, topic = null)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.90,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertNull(success.impact.cardId)
        assertNull(success.impact.topic)
        assertNull(success.impact.topicCode)
    }

    // 6. Mapping to existing Learning Memory aggregate projection
    @Test
    fun testMappingUpdatesMemoryRecordConfidenceCorrectly() {
        val initialMemory = createSampleMemory(cardConfidence = 0.50)
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.85, // triggers +0.10 confidence boost
            createdAt = 1600L
        )

        val result = integrator.integrate(
            interaction = interaction,
            feedback = feedback,
            currentMemory = initialMemory
        )

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        val updatedRecord = success.updatedMemory?.findRecord(sampleCardId)
        assertNotNull(updatedRecord)
        assertEquals(0.60, updatedRecord!!.confidence, 0.001)
    }

    @Test
    fun testRemediationSignalDecreasesMemoryRecordConfidence() {
        val initialMemory = createSampleMemory(cardConfidence = 0.50)
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 1, // triggers -0.05 reduction
            createdAt = 1600L
        )

        val result = integrator.integrate(
            interaction = interaction,
            feedback = feedback,
            currentMemory = initialMemory
        )

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        val updatedRecord = success.updatedMemory?.findRecord(sampleCardId)
        assertNotNull(updatedRecord)
        assertEquals(0.45, updatedRecord!!.confidence, 0.001)
    }

    // 7. Rejection tests: validation failure & user ID mismatch
    @Test
    fun testMismatchedInteractionIdIsRejected() {
        val interaction = createSampleInteraction(interactionId = "int_100")
        val feedback = AILearningFeedback(
            interactionId = "int_diff",
            usefulnessRating = 5,
            createdAt = 1600L
        )

        val result = integrator.integrate(interaction, feedback)

        assertTrue(result.isRejected)
        val rejected = result as AILearningMemoryIntegrationResult.Rejected
        assertEquals(AILearningMemoryIntegrationRejectionReason.FEEDBACK_VALIDATION_FAILED, rejected.reason)
    }

    @Test
    fun testUserIdMismatchIsRejected() {
        val interaction = createSampleInteraction(userId = sampleUserId)
        val otherUserMemory = createSampleMemory(userId = otherUserId)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            createdAt = 1600L
        )

        val result = integrator.integrate(
            interaction = interaction,
            feedback = feedback,
            currentMemory = otherUserMemory
        )

        assertTrue(result.isRejected)
        val rejected = result as AILearningMemoryIntegrationResult.Rejected
        assertEquals(AILearningMemoryIntegrationRejectionReason.USER_ID_MISMATCH, rejected.reason)
    }

    // 8. Integration from accepted feedback result
    @Test
    fun testIntegrateAcceptedFeedbackResult() {
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            createdAt = 1600L
        )
        val outcome = createSampleOutcome()
        val acceptedResult = AIInteractionFeedbackResult.Accepted(interaction, feedback, outcome)

        val memory = createSampleMemory(cardConfidence = 0.70)
        val result = integrator.integrateAccepted(acceptedResult, memory)

        assertTrue(result.isSuccess)
        val success = result as AILearningMemoryIntegrationResult.Success
        assertEquals("int_100", success.interactionId)
        assertNotNull(success.updatedMemory)
    }

    // 9. Determinism and Immutability
    @Test
    fun testDeterministicRepeatedCalculation() {
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            confidenceSignal = 0.8,
            createdAt = 1600L
        )

        val impact1 = calculator.calculateImpact(interaction, feedback)
        val impact2 = calculator.calculateImpact(interaction, feedback)

        assertEquals(impact1, impact2)
        assertEquals(impact1.hashCode(), impact2.hashCode())
    }

    @Test
    fun testImmutabilityOfInputs() {
        val memory = createSampleMemory(cardConfidence = 0.50)
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            createdAt = 1600L
        )

        val memoryCopy = memory.copy()
        val interactionCopy = interaction.copy()
        val feedbackCopy = feedback.copy()

        integrator.integrate(interaction, feedback, null, memory)

        assertEquals(memoryCopy, memory)
        assertEquals(interactionCopy, interaction)
        assertEquals(feedbackCopy, feedback)
    }
}
