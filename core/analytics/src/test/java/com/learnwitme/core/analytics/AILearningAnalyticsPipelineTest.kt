package com.learnwitme.core.analytics

import com.learnwitme.core.application.ai.feedback.AIInteractionFeedbackResult
import com.learnwitme.core.application.ai.feedback.AIInteractionType
import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningFeedbackRecorder
import com.learnwitme.core.application.ai.feedback.AILearningInteraction
import com.learnwitme.core.application.ai.feedback.AILearningInteractionPersistenceCoordinator
import com.learnwitme.core.application.ai.feedback.AILearningInteractionResult
import com.learnwitme.core.application.ai.feedback.AILearningMemoryFeedbackIntegrator
import com.learnwitme.core.application.ai.feedback.AILearningMemoryIntegrationResult
import com.learnwitme.core.application.ai.feedback.AILearningOutcome
import com.learnwitme.core.application.ai.feedback.AIOutcomeStatus
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.AILearningInteractionRepository
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AILearningAnalyticsPipelineTest {

    private lateinit var dispatcher: InMemoryAILearningAnalyticsDispatcher
    private lateinit var mapper: AILearningAnalyticsEventMapper
    private lateinit var pipeline: AILearningAnalyticsPipeline

    private val sampleUserId = UserId("usr_analytics_01")
    private val sampleCardId = KnowledgeCardId("card_analytics_01")
    private val sampleTopic = Topic("tp_analytics", "Analytics Topic", "Analytics Topic Description")

    private val sampleInteraction = AILearningInteraction(
        interactionId = "int_analytics_01",
        userId = sampleUserId,
        action = AIInteractionType.LESSON,
        cardId = sampleCardId,
        topic = sampleTopic,
        startedAt = 1000L,
        completedAt = 1500L,
        provider = AIProviderType.OPENAI,
        model = "gpt-4o",
        promptTokens = 120,
        completionTokens = 60,
        personalizationPreset = "LESSON_ONLY"
    )

    private val sampleOutcome = AILearningOutcome(
        interactionId = "int_analytics_01",
        status = AIOutcomeStatus.COMPLETED,
        summary = "Generated analytics lesson",
        completedAt = 1500L
    )

    private val sampleFeedback = AILearningFeedback(
        interactionId = "int_analytics_01",
        usefulnessRating = 5,
        confidenceSignal = 0.90,
        comment = "Sensitive private comment that should not leak to analytics raw text",
        createdAt = 1600L
    )

    private val sampleMemory = LearningMemory(
        id = LearningMemoryId("mem_analytics_01"),
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
        dispatcher = InMemoryAILearningAnalyticsDispatcher()
        mapper = AILearningAnalyticsEventMapper()
        pipeline = AILearningAnalyticsPipeline(dispatcher, mapper)
    }

    // 1. Interaction Event Mapping
    @Test
    fun testInteractionEventMapping() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = null,
            feedback = null
        )

        val events = pipeline.processAndDispatch(result)

        assertEquals(1, events.size)
        val event = events[0] as AILearningAnalyticsEvent.InteractionCompleted
        assertEquals("evt_interaction_int_analytics_01", event.eventId)
        assertEquals(1500L, event.timestamp)
        assertEquals("int_analytics_01", event.interactionId)
        assertEquals("usr_analytics_01", event.userId)
        assertEquals("LESSON", event.action)
        assertEquals("card_analytics_01", event.cardId)
        assertEquals("tp_analytics", event.topicCode)
        assertEquals("OPENAI", event.provider)
        assertEquals("gpt-4o", event.model)
        assertEquals(120, event.promptTokens)
        assertEquals(60, event.completionTokens)
        assertEquals(180, event.totalTokens)
        assertEquals("LESSON_ONLY", event.personalizationPreset)
    }

    // 2. Outcome Event Mapping
    @Test
    fun testOutcomeEventMapping() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = null
        )

        val events = pipeline.processAndDispatch(result)

        assertEquals(2, events.size)
        val outcomeEvent = events.filterIsInstance<AILearningAnalyticsEvent.OutcomeRecorded>().firstOrNull()
        assertNotNull(outcomeEvent)
        assertEquals("evt_outcome_int_analytics_01", outcomeEvent!!.eventId)
        assertEquals(1500L, outcomeEvent.timestamp)
        assertEquals("int_analytics_01", outcomeEvent.interactionId)
        assertEquals("usr_analytics_01", outcomeEvent.userId)
        assertEquals("COMPLETED", outcomeEvent.outcomeStatus)
        assertEquals("Generated analytics lesson", outcomeEvent.summary)
    }

    // 3. Feedback Event Mapping
    @Test
    fun testFeedbackEventMapping() = runBlocking {
        val recorder = AILearningFeedbackRecorder()
        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted

        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult
        )

        val events = pipeline.processAndDispatch(result)

        val feedbackEvent = events.filterIsInstance<AILearningAnalyticsEvent.FeedbackRecorded>().firstOrNull()
        assertNotNull(feedbackEvent)
        assertEquals("evt_feedback_int_analytics_01", feedbackEvent!!.eventId)
        assertEquals(1600L, feedbackEvent.timestamp)
        assertEquals("int_analytics_01", feedbackEvent.interactionId)
        assertEquals(5, feedbackEvent.usefulnessRating)
        assertEquals(0.90, feedbackEvent.confidenceSignal!!, 0.001)
        assertTrue(feedbackEvent.hasComment)
    }

    // 4. Memory Impact Event Mapping
    @Test
    fun testMemoryImpactEventMapping() = runBlocking {
        val recorder = AILearningFeedbackRecorder()
        val integrator = AILearningMemoryFeedbackIntegrator()

        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted
        val memoryResult = integrator.integrateAccepted(recordResult, sampleMemory) as AILearningMemoryIntegrationResult.Success

        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = memoryResult
        )

        val events = pipeline.processAndDispatch(result)

        assertEquals(4, events.size)
        val impactEvent = events.filterIsInstance<AILearningAnalyticsEvent.MemoryImpactRecorded>().firstOrNull()
        assertNotNull(impactEvent)
        assertEquals("evt_impact_int_analytics_01", impactEvent!!.eventId)
        assertEquals("int_analytics_01", impactEvent.interactionId)
        assertEquals("CONFIDENCE_BOOST", impactEvent.classification)
        assertEquals(0.10, impactEvent.confidenceDelta, 0.001)
        assertEquals("card_analytics_01", impactEvent.cardId)
        assertEquals("tp_analytics", impactEvent.topicCode)
    }

    // 5. Correlation via InteractionId
    @Test
    fun testCorrelationViaInteractionIdAcrossAllEvents() = runBlocking {
        val recorder = AILearningFeedbackRecorder()
        val integrator = AILearningMemoryFeedbackIntegrator()

        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted
        val memoryResult = integrator.integrateAccepted(recordResult, sampleMemory) as AILearningMemoryIntegrationResult.Success

        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = memoryResult
        )

        val events = pipeline.processAndDispatch(result)

        assertEquals(4, events.size)
        events.forEach { event ->
            assertEquals("int_analytics_01", event.interactionId)
            assertEquals("usr_analytics_01", event.userId)
            assertEquals("LESSON", event.action)
        }
    }

    // 6. Optional Artifact Handling
    @Test
    fun testOptionalArtifactHandlingWhenFeedbackOrMemoryImpactIsNull() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = null,
            feedbackResult = null,
            memoryIntegrationResult = null
        )

        val events = pipeline.processAndDispatch(result)

        assertEquals(2, events.size)
        assertTrue(events[0] is AILearningAnalyticsEvent.InteractionCompleted)
        assertTrue(events[1] is AILearningAnalyticsEvent.OutcomeRecorded)
    }

    // 7. Deterministic Event Generation
    @Test
    fun testDeterministicEventGeneration() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome
        )

        val events1 = mapper.mapToAnalyticsEvents(result)
        val events2 = mapper.mapToAnalyticsEvents(result)

        assertEquals(events1, events2)
    }

    // 8. Sensitive Data Exclusion
    @Test
    fun testSensitiveDataExclusion() = runBlocking {
        val recorder = AILearningFeedbackRecorder()
        val recordResult = recorder.recordFeedback(sampleInteraction, sampleFeedback, sampleOutcome) as AIInteractionFeedbackResult.Accepted

        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome,
            feedback = sampleFeedback,
            feedbackResult = recordResult
        )

        val events = pipeline.processAndDispatch(result)
        val feedbackEvent = events.filterIsInstance<AILearningAnalyticsEvent.FeedbackRecorded>().first()

        assertTrue(feedbackEvent.hasComment)
        // Verify raw comment string property is NOT present on the event data class
        val eventProperties = feedbackEvent.javaClass.declaredFields.map { it.name }
        assertFalse(eventProperties.contains("comment"))
    }

    // 9. Immutability of Events
    @Test
    fun testImmutabilityOfEvents() = runBlocking {
        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome
        )

        val events = pipeline.processAndDispatch(result)
        assertEquals(dispatcher.events, events)
    }

    // 10. Compatibility with Sprint 13.2 Implementation
    @Test
    fun testCompatibilityWithSprint132PersistenceCoordinator() = runBlocking {
        val repository = mockk<AILearningInteractionRepository>(relaxed = true)

        val coordinator = AILearningInteractionPersistenceCoordinator(
            repository = repository,
            onResultPersisted = { res -> pipeline.processAndDispatch(res) }
        )

        val result = AILearningInteractionResult(
            generatedResult = "dummy",
            interaction = sampleInteraction,
            outcome = sampleOutcome
        )

        val success = coordinator.persist(result)

        assertTrue(success)
        assertEquals(2, dispatcher.events.size)
        assertTrue(dispatcher.events[0] is AILearningAnalyticsEvent.InteractionCompleted)
        assertTrue(dispatcher.events[1] is AILearningAnalyticsEvent.OutcomeRecorded)
    }
}
