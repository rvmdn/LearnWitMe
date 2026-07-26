package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.ReviewQuality
import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningMemoryEngineTest {

    private class FakeClock(var time: Long) : Clock {
        override fun currentTimeMillis(): Long = time
    }

    private val cardToTopicMapper: (KnowledgeCardId) -> String = { id ->
        if (id.value.startsWith("kc_sql")) "tp_sql" else "tp_other"
    }

    @Test
    fun testProcessReview_successfulReview() {
        val engine = LearningMemoryEngine()
        val clock = FakeClock(1000L)
        val cardId = KnowledgeCardId("kc_sql_select")

        val memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // NEW -> LEARNING
        val quality = ReviewQuality(accuracy = 0.9, hintUsed = false, responseTimeMs = 1500L, confidenceRating = 0.8)
        val decision = engine.processReview(memory, cardId, quality, clock, cardToTopicMapper)

        assertEquals(MemoryState.NEW, decision.stateTransition.first)
        assertEquals(MemoryState.LEARNING, decision.stateTransition.second)
        assertTrue(decision.isRetentionImproved)
        assertFalse(decision.isWeakTopicDetected)
        assertEquals(0.9, decision.updatedRecord.retention, 0.0)
        assertEquals(0.8, decision.updatedRecord.confidence, 0.0)

        // Verify record reviews history timestamp
        assertEquals(1000L, decision.updatedRecord.reviews.first().timestamp)
    }

    @Test
    fun testProcessReview_failedReview() {
        val engine = LearningMemoryEngine()
        val clock = FakeClock(1000L)
        val cardId = KnowledgeCardId("kc_sql_select")

        val memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // Review with low accuracy (failed) -> NEW -> LEARNING (accuracy is checked inside applyReview, first review from NEW is always LEARNING)
        val quality1 = ReviewQuality(accuracy = 0.8, hintUsed = false, responseTimeMs = 1500L, confidenceRating = 0.8)
        var nextMemory = engine.processReview(memory, cardId, quality1, clock, cardToTopicMapper).updatedMemory

        // Second review failed -> LEARNING -> FORGOTTEN
        clock.time = 2000L
        val quality2 = ReviewQuality(accuracy = 0.3, hintUsed = true, responseTimeMs = 2000L, confidenceRating = 0.2)
        val decision = engine.processReview(nextMemory, cardId, quality2, clock, cardToTopicMapper)

        assertEquals(MemoryState.LEARNING, decision.stateTransition.first)
        assertEquals(MemoryState.FORGOTTEN, decision.stateTransition.second)
        assertFalse(decision.isRetentionImproved)
        assertTrue(decision.isWeakTopicDetected) // Since FORGOTTEN cards exist in tp_sql, it becomes weak
    }

    @Test
    fun testProcessReview_memoryRecovery() {
        val engine = LearningMemoryEngine()
        val clock = FakeClock(1000L)
        val cardId = KnowledgeCardId("kc_sql_select")

        var memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // Make it FORGOTTEN
        val quality1 = ReviewQuality(accuracy = 0.8, hintUsed = false, responseTimeMs = 1500L, confidenceRating = 0.8)
        memory = engine.processReview(memory, cardId, quality1, clock, cardToTopicMapper).updatedMemory
        memory = memory.markForgotten(cardId)
        assertEquals(MemoryState.FORGOTTEN, memory.findRecord(cardId)?.state)

        // Remedial review successful -> FORGOTTEN -> RECOVERED
        clock.time = 3000L
        val quality2 = ReviewQuality(accuracy = 0.9, hintUsed = false, responseTimeMs = 1200L, confidenceRating = 0.9)
        val decision = engine.processReview(memory, cardId, quality2, clock, cardToTopicMapper)

        assertEquals(MemoryState.FORGOTTEN, decision.stateTransition.first)
        assertEquals(MemoryState.RECOVERED, decision.stateTransition.second)
        assertTrue(decision.isRetentionImproved)
    }

    @Test
    fun testDecayMemory() {
        val engine = LearningMemoryEngine()
        val clock = FakeClock(1000L)
        val cardId = KnowledgeCardId("kc_sql_select")

        var memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // Initial setup
        val quality = ReviewQuality(accuracy = 0.9, hintUsed = false, responseTimeMs = 1500L, confidenceRating = 0.8)
        memory = engine.processReview(memory, cardId, quality, clock, cardToTopicMapper).updatedMemory

        // Decay after 10 seconds (stability * 3600 seconds makes it a very small decay, retention remains high)
        clock.time = 10010L
        var decayedMemory = engine.decayMemory(memory, cardId, clock)
        assertTrue(decayedMemory.findRecord(cardId)?.retention!! > 0.8)

        // Decay after a long time (stability is around 1.9 from accuracy, so 10 hours = 36000 seconds decay)
        clock.time = 1000L + 36000000L // 10,000 hours
        decayedMemory = engine.decayMemory(memory, cardId, clock)
        assertEquals(MemoryState.FORGOTTEN, decayedMemory.findRecord(cardId)?.state)
        assertEquals(0.0, decayedMemory.findRecord(cardId)?.retention!!, 0.0)
    }

    @Test
    fun testDeterministicBehaviour() {
        val engine = LearningMemoryEngine()
        val clock = FakeClock(1000L)
        val cardId = KnowledgeCardId("kc_sql_select")

        val memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )
        val quality = ReviewQuality(accuracy = 0.85, hintUsed = false, responseTimeMs = 1200L, confidenceRating = 0.8)

        val res1 = engine.processReview(memory, cardId, quality, clock, cardToTopicMapper)
        val res2 = engine.processReview(memory, cardId, quality, clock, cardToTopicMapper)

        // Output must be exactly the same for same input
        assertEquals(res1.updatedRecord.state, res2.updatedRecord.state)
        assertEquals(res1.updatedRecord.retention, res2.updatedRecord.retention, 0.0)
        assertEquals(res1.updatedRecord.stability, res2.updatedRecord.stability, 0.0)
        assertEquals(res1.isWeakTopicDetected, res2.isWeakTopicDetected)
    }
}
