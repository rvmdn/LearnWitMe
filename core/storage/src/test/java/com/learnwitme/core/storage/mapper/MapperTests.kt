package com.learnwitme.core.storage.mapper

import com.learnwitme.core.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTests {

    @Test
    fun testUserProfileMapping() {
        val original = UserProfile(
            id = UserId("user1"),
            careerPath = CareerPath("cp_dev", "Developer", "desc"),
            preference = LearningPreference(listOf("topic1", "topic2"), 45),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.SENIOR,
            progress = LearningProgress(10, 5, 3, 1000L)
        )
        
        val entity = original.toEntity()
        val mappedBack = entity.toDomain()
        
        assertEquals(original.id, mappedBack.id)
        assertEquals(original.careerPath, mappedBack.careerPath)
        assertEquals(original.preference, mappedBack.preference)
        assertEquals(original.currentLevel, mappedBack.currentLevel)
        assertEquals(original.targetLevel, mappedBack.targetLevel)
        assertEquals(original.progress, mappedBack.progress)
    }

    @Test
    fun testLearningMemoryMapping() {
        val reviews = listOf(MemoryReview(100L, 0.9, false, 0.8))
        val record = MemoryRecord(KnowledgeCardId("c1"), MemoryState.REVIEWING, 0.9, 1.5, 0.8, reviews)
        val original = LearningMemory(LearningMemoryId("mem1"), UserId("u1"), listOf(record))
        
        val entity = original.toEntity()
        val recordsEntity = original.records.map { it.toEntity(original.id.value) }
        
        val mappedBack = entity.toDomain(recordsEntity)
        
        assertEquals(original.id, mappedBack.id)
        assertEquals(original.userId, mappedBack.userId)
        assertEquals(1, mappedBack.records.size)
        
        val backRecord = mappedBack.records[0]
        assertEquals(record.cardId, backRecord.cardId)
        assertEquals(record.state, backRecord.state)
        assertEquals(record.retention, backRecord.retention, 0.001)
        assertEquals(record.reviews.size, backRecord.reviews.size)
        assertEquals(record.reviews[0].accuracy, backRecord.reviews[0].accuracy, 0.001)
    }
}
