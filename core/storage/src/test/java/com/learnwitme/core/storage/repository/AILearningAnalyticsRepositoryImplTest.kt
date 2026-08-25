package com.learnwitme.core.storage.repository

import com.learnwitme.core.analytics.AILearningAnalyticsEvent
import com.learnwitme.core.analytics.AnalyticsDeliveryStatus
import com.learnwitme.core.storage.local.dao.AILearningAnalyticsEventDao
import com.learnwitme.core.storage.local.entity.AILearningAnalyticsEventEntity
import com.learnwitme.core.storage.mapper.AILearningAnalyticsStorageMapper
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

class AILearningAnalyticsRepositoryImplTest {

    private lateinit var dao: AILearningAnalyticsEventDao
    private lateinit var repository: AILearningAnalyticsRepositoryImpl

    private val sampleEvent = AILearningAnalyticsEvent.InteractionCompleted(
        eventId = "evt_interaction_test_200",
        timestamp = 2000L,
        interactionId = "int_test_200",
        userId = "usr_test_200",
        action = "LESSON",
        cardId = "card_200",
        topicCode = "tp_kotlin",
        provider = "OPENAI",
        model = "gpt-4o",
        promptTokens = 100,
        completionTokens = 50,
        totalTokens = 150,
        personalizationPreset = "FULL"
    )

    private val sampleFeedbackEvent = AILearningAnalyticsEvent.FeedbackRecorded(
        eventId = "evt_feedback_test_200",
        timestamp = 2100L,
        interactionId = "int_test_200",
        userId = "usr_test_200",
        action = "LESSON",
        usefulnessRating = 5,
        confidenceSignal = 0.95,
        hasComment = true
    )

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        repository = AILearningAnalyticsRepositoryImpl(dao)
    }

    // 1. Enqueue Delegates to DAO Insert
    @Test
    fun testEnqueueDelegatesToDaoInsert() = runBlocking {
        repository.enqueue(listOf(sampleEvent))

        coVerify(exactly = 1) { dao.insertAll(any()) }
    }

    // 2. Retrieve Pending Events
    @Test
    fun testGetPendingEventsMapsEntitiesToQueuedEvents() = runBlocking {
        val entity = AILearningAnalyticsStorageMapper.toEntity(sampleEvent)
        coEvery { dao.getPendingEvents(10) } returns listOf(entity)

        val pending = repository.getPendingEvents(10)

        assertEquals(1, pending.size)
        assertEquals("evt_interaction_test_200", pending[0].event.eventId)
        assertEquals(AnalyticsDeliveryStatus.PENDING, pending[0].deliveryStatus)
    }

    // 3. Mark Delivered
    @Test
    fun testMarkDeliveredDelegatesToDao() = runBlocking {
        repository.markDelivered(listOf("evt_interaction_test_200"))

        coVerify(exactly = 1) { dao.markDelivered(listOf("evt_interaction_test_200")) }
    }

    // 4. Mark Failed
    @Test
    fun testMarkFailedDelegatesToDaoWithMetadata() = runBlocking {
        repository.markFailed("evt_interaction_test_200", "Connection Timeout", 5000L)

        coVerify(exactly = 1) {
            dao.markFailed(
                eventId = "evt_interaction_test_200",
                lastAttemptAt = any(),
                nextRetryAt = 5000L,
                errorMessage = "Connection Timeout"
            )
        }
    }

    // 5. Mapper Round-Trip: Interaction Event
    @Test
    fun testMapperRoundTripInteractionCompleted() {
        val entity = AILearningAnalyticsStorageMapper.toEntity(sampleEvent)
        val queued = AILearningAnalyticsStorageMapper.toQueuedEvent(entity)

        val restoredEvent = queued.event as AILearningAnalyticsEvent.InteractionCompleted
        assertEquals(sampleEvent.eventId, restoredEvent.eventId)
        assertEquals(sampleEvent.interactionId, restoredEvent.interactionId)
        assertEquals(sampleEvent.userId, restoredEvent.userId)
        assertEquals(sampleEvent.promptTokens, restoredEvent.promptTokens)
    }

    // 6. Mapper Round-Trip: Feedback Event & Sensitive Data Exclusion
    @Test
    fun testMapperRoundTripFeedbackRecorded() {
        val entity = AILearningAnalyticsStorageMapper.toEntity(sampleFeedbackEvent)
        val queued = AILearningAnalyticsStorageMapper.toQueuedEvent(entity)

        val restoredEvent = queued.event as AILearningAnalyticsEvent.FeedbackRecorded
        assertEquals(sampleFeedbackEvent.eventId, restoredEvent.eventId)
        assertEquals(5, restoredEvent.usefulnessRating)
        assertTrue(restoredEvent.hasComment)

        // Verify entity does NOT contain raw prompt/comment string properties
        val entityFields = entity.javaClass.declaredFields.map { it.name }
        assertFalse(entityFields.contains("prompt"))
        assertFalse(entityFields.contains("comment"))
    }
}
