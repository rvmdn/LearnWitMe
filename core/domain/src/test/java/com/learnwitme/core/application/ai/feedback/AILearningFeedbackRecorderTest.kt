package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AILearningFeedbackRecorderTest {

    private val recorder = AILearningFeedbackRecorder()
    private val sampleUserId = UserId("usr_test_123")
    private val sampleCardId = KnowledgeCardId("card_k1")
    private val sampleTopic = Topic("tp_kotlin", "Kotlin Basics", "Basic Kotlin Programming Concepts")

    private fun createSampleInteraction(
        interactionId: String = "int_100",
        startedAt: Long = 1000L,
        completedAt: Long? = 1500L
    ): AILearningInteraction {
        return AILearningInteraction(
            interactionId = interactionId,
            userId = sampleUserId,
            action = AIInteractionType.LESSON,
            cardId = sampleCardId,
            topic = sampleTopic,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = AIProviderType.GEMINI,
            model = "gemini-1.5-pro",
            promptTokens = 150,
            completionTokens = 80,
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
            summary = "Summary for status $status",
            completedAt = completedAt
        )
    }

    // 1. Valid feedback recording
    @Test
    fun testValidFeedbackRecordingWithFeedbackObject() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.95,
            comment = "Excellent explanation of coroutines",
            createdAt = 1600L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isAccepted)
        assertFalse(result.isRejected)
        val accepted = result as AIInteractionFeedbackResult.Accepted
        assertEquals(interaction, accepted.interaction)
        assertEquals(feedback, accepted.feedback)
        assertEquals(outcome, accepted.outcome)
    }

    @Test
    fun testValidFeedbackRecordingWithParameters() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome()

        val result = recorder.recordFeedback(
            interaction = interaction,
            usefulnessRating = 4,
            confidenceSignal = 0.85,
            comment = "Very helpful",
            createdAt = 1550L,
            outcome = outcome
        )

        assertTrue(result.isAccepted)
        val accepted = result as AIInteractionFeedbackResult.Accepted
        assertEquals(interaction.interactionId, accepted.feedback.interactionId)
        assertEquals(4, accepted.feedback.usefulnessRating)
        assertEquals(0.85, accepted.feedback.confidenceSignal!!, 0.001)
        assertEquals("Very helpful", accepted.feedback.comment)
        assertEquals(1550L, accepted.feedback.createdAt)
    }

    // 2. Mismatched interactionId
    @Test
    fun testMismatchedFeedbackInteractionId() {
        val interaction = createSampleInteraction(interactionId = "int_100")
        val feedback = AILearningFeedback(
            interactionId = "int_999",
            usefulnessRating = 4,
            createdAt = 2000L
        )

        val result = recorder.recordFeedback(interaction, feedback)

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INTERACTION_ID_MISMATCH, rejected.reason)
        assertEquals("int_100", rejected.interactionId)
    }

    @Test
    fun testMismatchedOutcomeInteractionId() {
        val interaction = createSampleInteraction(interactionId = "int_100")
        val outcome = createSampleOutcome(interactionId = "int_diff")
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            createdAt = 2000L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INTERACTION_ID_MISMATCH, rejected.reason)
    }

    // 3. Invalid usefulness rating
    @Test
    fun testInvalidUsefulnessRatingUnderflow() {
        val interaction = createSampleInteraction()

        val result = recorder.recordFeedback(
            interaction = interaction,
            usefulnessRating = 0,
            createdAt = 2000L
        )

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING, rejected.reason)
    }

    @Test
    fun testInvalidUsefulnessRatingOverflow() {
        val interaction = createSampleInteraction()

        val result = recorder.recordFeedback(
            interaction = interaction,
            usefulnessRating = 6,
            createdAt = 2000L
        )

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING, rejected.reason)
    }

    // 4. Invalid confidence signal
    @Test
    fun testInvalidConfidenceSignalUnderflow() {
        val interaction = createSampleInteraction()

        val result = recorder.recordFeedback(
            interaction = interaction,
            usefulnessRating = 4,
            confidenceSignal = -0.1,
            createdAt = 2000L
        )

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_CONFIDENCE_SIGNAL, rejected.reason)
    }

    @Test
    fun testInvalidConfidenceSignalOverflow() {
        val interaction = createSampleInteraction()

        val result = recorder.recordFeedback(
            interaction = interaction,
            usefulnessRating = 4,
            confidenceSignal = 1.1,
            createdAt = 2000L
        )

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_CONFIDENCE_SIGNAL, rejected.reason)
    }

    // 5. Invalid timestamp ordering
    @Test
    fun testFeedbackCreatedAtEarlierThanStartedAt() {
        val interaction = createSampleInteraction(startedAt = 1000L, completedAt = 1500L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            createdAt = 900L
        )

        val result = recorder.recordFeedback(interaction, feedback)

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING, rejected.reason)
    }

    @Test
    fun testFeedbackCreatedAtEarlierThanCompletedAt() {
        val interaction = createSampleInteraction(startedAt = 1000L, completedAt = 1500L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            createdAt = 1200L
        )

        val result = recorder.recordFeedback(interaction, feedback)

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING, rejected.reason)
    }

    @Test
    fun testFeedbackCreatedAtEarlierThanOutcomeCompletedAt() {
        val interaction = createSampleInteraction(startedAt = 1000L, completedAt = 1200L)
        val outcome = createSampleOutcome(interactionId = "int_100", completedAt = 1600L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 4,
            createdAt = 1400L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isRejected)
        val rejected = result as AIInteractionFeedbackResult.Rejected
        assertEquals(AIInteractionFeedbackRejectionReason.INVALID_TIMESTAMP_ORDERING, rejected.reason)
    }

    // 6. Completed interaction
    @Test
    fun testCompletedInteractionFeedback() {
        val interaction = createSampleInteraction(completedAt = 1500L)
        val outcome = createSampleOutcome(status = AIOutcomeStatus.COMPLETED, completedAt = 1500L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 1.0,
            comment = "Fully mastered",
            createdAt = 1600L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isAccepted)
        val accepted = result as AIInteractionFeedbackResult.Accepted
        assertEquals(AIOutcomeStatus.COMPLETED, accepted.outcome?.status)
    }

    // 7. Failed interaction
    @Test
    fun testFailedInteractionFeedback() {
        val interaction = createSampleInteraction(completedAt = 1500L)
        val outcome = createSampleOutcome(status = AIOutcomeStatus.FAILED, completedAt = 1500L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 2,
            confidenceSignal = 0.3,
            comment = "Quiz generated too difficult",
            createdAt = 1600L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isAccepted)
        val accepted = result as AIInteractionFeedbackResult.Accepted
        assertEquals(AIOutcomeStatus.FAILED, accepted.outcome?.status)
        assertEquals(2, accepted.feedback.usefulnessRating)
    }

    // 8. Skipped interaction
    @Test
    fun testSkippedInteractionFeedback() {
        val interaction = createSampleInteraction(completedAt = 1200L)
        val outcome = createSampleOutcome(status = AIOutcomeStatus.SKIPPED, completedAt = 1200L)
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 3,
            confidenceSignal = 0.5,
            comment = "Skipped by learner",
            createdAt = 1300L
        )

        val result = recorder.recordFeedback(interaction, feedback, outcome)

        assertTrue(result.isAccepted)
        val accepted = result as AIInteractionFeedbackResult.Accepted
        assertEquals(AIOutcomeStatus.SKIPPED, accepted.outcome?.status)
    }

    // 9. Deterministic repeated recording
    @Test
    fun testDeterministicRepeatedRecording() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            confidenceSignal = 0.9,
            createdAt = 1600L
        )

        val result1 = recorder.recordFeedback(interaction, feedback, outcome)
        val result2 = recorder.recordFeedback(interaction, feedback, outcome)

        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    // 10. Immutability / original object remains unchanged
    @Test
    fun testOriginalObjectsRemainUnchanged() {
        val interaction = createSampleInteraction()
        val outcome = createSampleOutcome()
        val feedback = AILearningFeedback(
            interactionId = "int_100",
            usefulnessRating = 5,
            createdAt = 1600L
        )

        val interactionCopy = interaction.copy()
        val outcomeCopy = outcome.copy()
        val feedbackCopy = feedback.copy()

        recorder.recordFeedback(interaction, feedback, outcome)

        assertEquals(interactionCopy, interaction)
        assertEquals(outcomeCopy, outcome)
        assertEquals(feedbackCopy, feedback)
    }

    // 11. Equality of identical results
    @Test
    fun testEqualityOfIdenticalResults() {
        val interaction = createSampleInteraction()
        val feedback = AILearningFeedback(interactionId = "int_100", usefulnessRating = 4, createdAt = 1600L)

        val accepted1 = AIInteractionFeedbackResult.Accepted(interaction, feedback)
        val accepted2 = AIInteractionFeedbackResult.Accepted(interaction, feedback)
        val rejected1 = AIInteractionFeedbackResult.Rejected(
            AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING,
            "Invalid rating",
            "int_100"
        )
        val rejected2 = AIInteractionFeedbackResult.Rejected(
            AIInteractionFeedbackRejectionReason.INVALID_USEFULNESS_RATING,
            "Invalid rating",
            "int_100"
        )

        assertEquals(accepted1, accepted2)
        assertEquals(accepted1.hashCode(), accepted2.hashCode())
        assertEquals(rejected1, rejected2)
        assertEquals(rejected1.hashCode(), rejected2.hashCode())
        assertNotEquals(accepted1, rejected1)
    }
}
