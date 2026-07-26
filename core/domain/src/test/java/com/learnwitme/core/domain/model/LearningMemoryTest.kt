package com.learnwitme.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LearningMemoryTest {

    @Test
    fun testLearningMemory_creationAndInvariants() {
        val memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1"),
            records = emptyList()
        )

        assertEquals(LearningMemoryId("mem_1"), memory.id)
        assertEquals(UserId("usr_1"), memory.userId)
        assertTrue(memory.records.isEmpty())

        // Try creating with duplicate records
        val record1 = MemoryRecord(
            cardId = KnowledgeCardId("kc_1"),
            state = MemoryState.NEW,
            retention = 1.0,
            stability = 1.0,
            confidence = 1.0,
            reviews = emptyList()
        )

        try {
            LearningMemory(
                id = LearningMemoryId("mem_1"),
                userId = UserId("usr_1"),
                records = listOf(record1, record1)
            )
            fail("Expected IllegalArgumentException for duplicate records")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot contain duplicate records"))
        }
    }

    @Test
    fun testMemoryRecord_transitionsAndReviews() {
        val cardId = KnowledgeCardId("kc_1")
        var memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // 1. Record first review (NEW -> LEARNING)
        val review1 = MemoryReview(timestamp = 1000L, accuracy = 0.8, hintUsed = false, confidenceRating = 0.7)
        memory = memory.recordReview(cardId, review1)

        val record = memory.findRecord(cardId)
        assertNotNull(record)
        assertEquals(MemoryState.LEARNING, record?.state)
        assertEquals(0.8, record?.retention!!, 0.0)
        assertEquals(0.7, record.confidence, 0.0)
        assertEquals(1, record.reviews.size)

        // 2. Record second successful review (LEARNING -> REVIEWING)
        val review2 = MemoryReview(timestamp = 2000L, accuracy = 0.9, hintUsed = false, confidenceRating = 0.8)
        memory = memory.recordReview(cardId, review2)
        assertEquals(MemoryState.REVIEWING, memory.findRecord(cardId)?.state)

        // 3. Mark forgotten (REVIEWING -> FORGOTTEN)
        memory = memory.markForgotten(cardId)
        assertEquals(MemoryState.FORGOTTEN, memory.findRecord(cardId)?.state)
        assertEquals(0.0, memory.findRecord(cardId)?.retention!!, 0.0)

        // 4. Recover (FORGOTTEN -> RECOVERED)
        val review3 = MemoryReview(timestamp = 3000L, accuracy = 0.95, hintUsed = false, confidenceRating = 0.9)
        memory = memory.recover(cardId, review3)
        assertEquals(MemoryState.RECOVERED, memory.findRecord(cardId)?.state)
    }

    @Test
    fun testMemoryRecord_invalidTransitions() {
        val record = MemoryRecord(
            cardId = KnowledgeCardId("kc_1"),
            state = MemoryState.NEW,
            retention = 1.0,
            stability = 1.0,
            confidence = 1.0,
            reviews = emptyList()
        )

        // Try invalid transition from NEW to MASTERED directly
        try {
            record.transitionTo(MemoryState.MASTERED)
            fail("Expected IllegalArgumentException for invalid transition")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Illegal memory state transition"))
        }

        // Try invalid transition from FORGOTTEN to MASTERED directly
        val forgottenRecord = record.copy(state = MemoryState.FORGOTTEN)
        try {
            forgottenRecord.transitionTo(MemoryState.MASTERED)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Illegal memory state transition"))
        }
    }

    @Test
    fun testMemoryRecord_retentionAndConfidenceValidation() {
        // 1. Invalid Retention > 1.0
        try {
            MemoryRecord(
                cardId = KnowledgeCardId("kc_1"),
                state = MemoryState.NEW,
                retention = 1.5,
                stability = 1.0,
                confidence = 1.0,
                reviews = emptyList()
            )
            fail("Expected IllegalArgumentException for retention > 1.0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Retention must be between 0.0 and 1.0"))
        }

        // 2. Invalid Confidence < 0.0
        try {
            MemoryRecord(
                cardId = KnowledgeCardId("kc_1"),
                state = MemoryState.NEW,
                retention = 1.0,
                stability = 1.0,
                confidence = -0.1,
                reviews = emptyList()
            )
            fail("Expected IllegalArgumentException for confidence < 0.0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Confidence must be between 0.0 and 1.0"))
        }
    }

    @Test
    fun testLearningMemory_weakTopicDetection() {
        val cardId1 = KnowledgeCardId("kc_sql_select")
        val cardId2 = KnowledgeCardId("kc_sql_join")

        var memory = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        // Map kc_sql_select and kc_sql_join to "tp_sql"
        val mapper: (KnowledgeCardId) -> String = { id ->
            if (id.value.startsWith("kc_sql")) "tp_sql" else "tp_other"
        }

        // Create standard record
        val review = MemoryReview(1000L, 0.9, false, 0.8)
        memory = memory.recordReview(cardId1, review)
        memory = memory.recordReview(cardId2, review)

        // Average retention is high, not weak
        assertTrue(memory.detectWeakTopics(mapper).isEmpty())

        // Mark one card forgotten -> makes topic weak
        memory = memory.markForgotten(cardId1)
        val weakTopics = memory.detectWeakTopics(mapper)
        assertEquals(1, weakTopics.size)
        assertTrue(weakTopics.contains("tp_sql"))
    }

    @Test
    fun testLearningMemory_equality() {
        val mem1 = LearningMemory(
            id = LearningMemoryId("mem_1"),
            userId = UserId("usr_1")
        )

        val mem2 = mem1.copy(records = listOf(
            MemoryRecord(KnowledgeCardId("kc_1"), MemoryState.NEW, 1.0, 1.0, 1.0)
        ))
        assertEquals(mem1.id, mem2.id)

        val mem3 = mem1.copy(id = LearningMemoryId("mem_2"))
        assertNotEquals(mem1.id, mem3.id)
    }
}
