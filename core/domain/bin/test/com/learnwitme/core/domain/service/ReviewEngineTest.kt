package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryReview
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.ReviewPolicy
import com.learnwitme.core.domain.model.ReviewPriority
import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewEngineTest {

    private class FakeClock(var time: Long) : Clock {
        override fun currentTimeMillis(): Long = time
    }

    @Test
    fun testCalculateDecision_overdueAndCritical() {
        val engine = ReviewEngine()
        val clock = FakeClock(8000000L) // Overdue since nextReviewTime is 7380000L
        val policy = ReviewPolicy()

        val record = MemoryRecord(
            cardId = KnowledgeCardId("kc_1"),
            state = MemoryState.REVIEWING,
            retention = 0.55, // Low retention
            stability = 2.0,
            confidence = 0.8,
            reviews = listOf(MemoryReview(timestamp = 900000L, accuracy = 0.85, hintUsed = false, confidenceRating = 0.8))
        )

        val decision = engine.calculateDecision(record, clock, policy)

        assertTrue(decision.isOverdue)
        assertEquals(ReviewPriority.CRITICAL, decision.priority)
        assertFalse(decision.canBeDeferred)
        assertTrue(decision.reason.contains("below critical threshold"))
    }

    @Test
    fun testCalculateDecision_futureLowPriority() {
        val engine = ReviewEngine()
        val clock = FakeClock(1000000L)
        val policy = ReviewPolicy()

        val record = MemoryRecord(
            cardId = KnowledgeCardId("kc_1"),
            state = MemoryState.REMEMBERED,
            retention = 0.95, // High retention
            stability = 10.0,
            confidence = 0.9,
            reviews = listOf(MemoryReview(timestamp = 999900L, accuracy = 0.95, hintUsed = false, confidenceRating = 0.9))
        )

        val decision = engine.calculateDecision(record, clock, policy)

        assertFalse(decision.isOverdue)
        assertEquals(ReviewPriority.LOW, decision.priority)
        assertTrue(decision.canBeDeferred)
    }

    @Test
    fun testDetermineQueue_sortingOrder() {
        val engine = ReviewEngine()
        val clock = FakeClock(10000000L) // Overdue for Card 1 (3200000L) and Card 2 (7280000L)
        val policy = ReviewPolicy()

        // Card 1: Forgotten (CRITICAL)
        val record1 = MemoryRecord(
            cardId = KnowledgeCardId("kc_forgotten"),
            state = MemoryState.FORGOTTEN,
            retention = 0.0,
            stability = 1.0,
            confidence = 0.5,
            reviews = listOf(MemoryReview(500000L, 0.3, true, 0.4))
        )

        // Card 2: Overdue but retention is fine (HIGH)
        val record2 = MemoryRecord(
            cardId = KnowledgeCardId("kc_overdue"),
            state = MemoryState.REMEMBERED,
            retention = 0.9,
            stability = 2.0,
            confidence = 0.8,
            reviews = listOf(MemoryReview(800000L, 0.9, false, 0.8))
        )

        // Card 3: Stable, next review is far in future (LOW)
        val record3 = MemoryRecord(
            cardId = KnowledgeCardId("kc_stable"),
            state = MemoryState.MASTERED,
            retention = 0.95,
            stability = 10.0,
            confidence = 0.9,
            reviews = listOf(MemoryReview(999900L, 0.95, false, 0.9))
        )

        val memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1"),
            records = listOf(record3, record1, record2) // unsorted mix
        )

        val queue = engine.determineQueue(memory, clock, policy)

        assertEquals(3, queue.decisions.size)
        // Verify sorting order: CRITICAL -> HIGH -> LOW
        assertEquals("kc_forgotten", queue.decisions[0].cardId.value)
        assertEquals(ReviewPriority.CRITICAL, queue.decisions[0].priority)

        assertEquals("kc_overdue", queue.decisions[1].cardId.value)
        assertEquals(ReviewPriority.HIGH, queue.decisions[1].priority)

        assertEquals("kc_stable", queue.decisions[2].cardId.value)
        assertEquals(ReviewPriority.LOW, queue.decisions[2].priority)
    }

    @Test
    fun testDeterministicBehaviour() {
        val engine = ReviewEngine()
        val clock = FakeClock(1000000L)
        val policy = ReviewPolicy()

        val record = MemoryRecord(
            cardId = KnowledgeCardId("kc_1"),
            state = MemoryState.REMEMBERED,
            retention = 0.85,
            stability = 5.0,
            confidence = 0.8,
            reviews = listOf(MemoryReview(950000L, 0.85, false, 0.8))
        )

        val res1 = engine.calculateDecision(record, clock, policy)
        val res2 = engine.calculateDecision(record, clock, policy)

        assertEquals(res1.nextReviewTime, res2.nextReviewTime)
        assertEquals(res1.priority, res2.priority)
        assertEquals(res1.isOverdue, res2.isOverdue)
        assertEquals(res1.canBeDeferred, res2.canBeDeferred)
    }
}
