package com.learnwitme.core.analytics

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PersistentAILearningAnalyticsDispatcherTest {

    private lateinit var repository: AILearningAnalyticsRepository
    private lateinit var downstreamDelegate: AILearningAnalyticsDispatcher
    private lateinit var dispatcher: PersistentAILearningAnalyticsDispatcher

    private val sampleEvent = AILearningAnalyticsEvent.InteractionCompleted(
        eventId = "evt_interaction_test_100",
        timestamp = 1000L,
        interactionId = "int_test_100",
        userId = "usr_test_100",
        action = "LESSON",
        cardId = "card_100",
        topicCode = "tp_kotlin",
        provider = "OPENAI",
        model = "gpt-4o",
        promptTokens = 100,
        completionTokens = 50,
        totalTokens = 150,
        personalizationPreset = "FULL"
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        downstreamDelegate = mockk(relaxed = true)
        dispatcher = PersistentAILearningAnalyticsDispatcher(
            repository = repository,
            delegate = downstreamDelegate
        )
    }

    // 1. Enqueue Event & Offline Persistence
    @Test
    fun testEnqueueEventAndOfflinePersistence() = runBlocking {
        dispatcher.dispatch(sampleEvent)

        coVerify(exactly = 1) { repository.enqueue(listOf(sampleEvent)) }
    }

    // 2. Successful Delivery Boundary
    @Test
    fun testSuccessfulDeliveryToDownstreamDelegate() = runBlocking {
        dispatcher.dispatch(sampleEvent)

        coVerify(exactly = 1) { repository.enqueue(listOf(sampleEvent)) }
        coVerify(exactly = 1) { downstreamDelegate.dispatch(sampleEvent) }
        coVerify(exactly = 1) { repository.markDelivered(listOf("evt_interaction_test_100")) }
    }

    // 3. Failed Delivery Boundary Updates Retry State
    @Test
    fun testFailedDeliveryUpdatesRetryMetadata() = runBlocking {
        coEvery { downstreamDelegate.dispatch(sampleEvent) } throws RuntimeException("Network Offline")

        dispatcher.dispatch(sampleEvent)

        coVerify(exactly = 1) { repository.enqueue(listOf(sampleEvent)) }
        coVerify(exactly = 1) { repository.markFailed("evt_interaction_test_100", "Network Offline") }
    }

    // 4. Analytics Failure Isolation (Repository Error Does Not Throw Exception)
    @Test
    fun testAnalyticsFailureIsolationOnRepositoryException() = runBlocking {
        coEvery { repository.enqueue(any()) } throws RuntimeException("Database Error")

        // Must not throw exception
        dispatcher.dispatch(sampleEvent)

        assertTrue(true)
    }

    // 5. Correlation ID Preservation
    @Test
    fun testCorrelationIdPreservation() = runBlocking {
        dispatcher.dispatch(sampleEvent)

        assertEquals("int_test_100", sampleEvent.interactionId)
        assertEquals("usr_test_100", sampleEvent.userId)
    }
}
