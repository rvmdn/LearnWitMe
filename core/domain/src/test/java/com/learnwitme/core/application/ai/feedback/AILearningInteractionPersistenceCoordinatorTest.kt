package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.AILearningInteractionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AILearningInteractionPersistenceCoordinatorTest {

    private val repository = mockk<AILearningInteractionRepository>(relaxed = true)
    private lateinit var coordinator: AILearningInteractionPersistenceCoordinator

    private val sampleUserId = UserId("usr_01")
    private val sampleCardId = KnowledgeCardId("card_01")
    private val sampleTopic = Topic("tp_kotlin", "Kotlin Basics", "Kotlin Description")

    private val sampleInteraction = AILearningInteraction(
        interactionId = "int_01",
        userId = sampleUserId,
        action = AIInteractionType.LESSON,
        cardId = sampleCardId,
        topic = sampleTopic,
        startedAt = 1000L,
        completedAt = 1500L,
        provider = AIProviderType.GEMINI,
        model = "gemini-1.5-pro",
        promptTokens = 100,
        completionTokens = 50,
        personalizationPreset = "LESSON_ONLY"
    )

    private val sampleOutcome = AILearningOutcome(
        interactionId = "int_01",
        status = AIOutcomeStatus.COMPLETED,
        summary = "Lesson completed",
        completedAt = 1500L
    )

    private val sampleFeedback = AILearningFeedback(
        interactionId = "int_01",
        usefulnessRating = 5,
        confidenceSignal = 0.85,
        comment = "Great lesson",
        createdAt = 1600L
    )

    private val sampleMemory = LearningMemory(
        id = LearningMemoryId("mem_01"),
        userId = sampleUserId,
        records = listOf(
            MemoryRecord(
                cardId = sampleCardId,
                state = MemoryState.LEARNING,
                retention = 0.80,
                stability = 1.0,
                confidence = 0.50
            )
        )
    )

    @Before
    fun setUp() {
        coordinator = AILearningInteractionPersistenceCoordinator(repository)
    }

    // 1. Interaction-only persistence
    @Test
    fun testInteractionOnlyPersistence() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = null,
            feedback = null
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = null,
                feedback = null,
                impact = null
            )
        }
    }

    // 2. Interaction + outcome persistence
    @Test
    fun testInteractionAndOutcomePersistence() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = null
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = sampleOutcome,
                feedback = null,
                impact = null
            )
        }
    }

    // 3. Interaction + feedback + outcome persistence
    @Test
    fun testInteractionFeedbackAndOutcomePersistence() = runBlocking {
        val feedbackResult = AIInteractionFeedbackResult.Accepted(
            interaction = sampleInteraction,
            feedback = sampleFeedback,
            outcome = sampleOutcome
        )
        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = feedbackResult,
            memoryIntegrationResult = null
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = sampleOutcome,
                feedback = sampleFeedback,
                impact = null
            )
        }
    }

    // 4. Complete feedback + memory impact persistence
    @Test
    fun testCompleteFeedbackAndMemoryImpactPersistence() = runBlocking {
        val recorder = AILearningFeedbackRecorder()
        val integrator = AILearningMemoryFeedbackIntegrator()

        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted
        val memoryResult = integrator.integrateAccepted(recordResult, sampleMemory) as AILearningMemoryIntegrationResult.Success

        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = memoryResult
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        assertNotNull(memoryResult.impact)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = sampleOutcome,
                feedback = sampleFeedback,
                impact = memoryResult.impact
            )
        }
    }

    // 5. Rejected feedback persistence
    @Test
    fun testRejectedFeedbackDoesNotPersistFeedbackOrMemoryImpact() = runBlocking {
        val rejectedFeedback = sampleFeedback.copy(interactionId = "mismatched_id")
        val recorder = AILearningFeedbackRecorder()

        val recordResult = recorder.recordFeedback(sampleInteraction, rejectedFeedback, sampleOutcome) as AIInteractionFeedbackResult.Rejected

        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = rejectedFeedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = null
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = sampleOutcome,
                feedback = null,
                impact = null
            )
        }
    }

    // 6. Rejected memory integration persistence
    @Test
    fun testRejectedMemoryIntegrationPersistsAcceptedFeedbackButNotMemoryImpact() = runBlocking {
        val mismatchedMemory = sampleMemory.copy(userId = UserId("usr_other"))
        val recorder = AILearningFeedbackRecorder()
        val integrator = AILearningMemoryFeedbackIntegrator()

        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted
        val memoryResult = integrator.integrateAccepted(recordResult, mismatchedMemory) as AILearningMemoryIntegrationResult.Rejected

        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = memoryResult
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = sampleInteraction,
                outcome = sampleOutcome,
                feedback = sampleFeedback,
                impact = null
            )
        }
    }

    // 7. InteractionId relationship validation
    @Test
    fun testMismatchedOutcomeInteractionIdThrowsException() = runBlocking {
        val mismatchedOutcome = sampleOutcome.copy(interactionId = "different_id")
        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = mismatchedOutcome,
            feedback = null
        )

        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                coordinator.persist(result)
            }
        }
        Unit
    }

    // 8. Repository delegation
    @Test
    fun testRepositoryDelegationOccursForServiceWithRepositoryConstructor() = runBlocking {
        val aiManager = mockk<com.learnwitme.core.domain.ai.AIManager>()
        val defaultResponse = com.learnwitme.core.domain.ai.AIResponse(
            content = "Lesson content",
            tokensUsed = 100,
            finishReason = "STOP",
            modelId = "gemini-1.5-pro",
            executionTimeMs = 150L
        )
        coEvery { aiManager.generateLesson(any(), any()) } returns defaultResponse

        val service = AILearningInteractionService(
            aiManager = aiManager,
            repository = repository
        )

        val result = service.executeLesson(
            interactionId = "int_01",
            userId = sampleUserId,
            topic = "Kotlin Basics",
            startedAt = 1000L,
            completedAt = 1500L
        )

        assertNotNull(result.generatedResult)
        coVerify(exactly = 1) {
            repository.saveArtifacts(
                interaction = result.interaction,
                outcome = result.outcome,
                feedback = null,
                impact = null
            )
        }
    }

    // 9. Persistence result correctness
    @Test
    fun testPersistenceResultCorrectness() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome
        )

        val success = coordinator.persist(result)
        assertTrue(success)
    }

    // 10. Immutability of original domain objects
    @Test
    fun testOriginalDomainObjectsRemainImmutableAfterPersistence() = runBlocking {
        val originalInteraction = sampleInteraction.copy()
        val originalOutcome = sampleOutcome.copy()
        val originalFeedback = sampleFeedback.copy()

        val result = AILearningInteractionResult(
            generatedResult = "dummy_result",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback
        )

        coordinator.persist(result)

        assertEquals(originalInteraction, sampleInteraction)
        assertEquals(originalOutcome, sampleOutcome)
        assertEquals(originalFeedback, sampleFeedback)
    }
}
