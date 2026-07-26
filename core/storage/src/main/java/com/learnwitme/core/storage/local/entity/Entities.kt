package com.learnwitme.core.storage.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val careerPathCode: String,
    val careerPathName: String,
    val careerPathDescription: String,
    val learningGoalJson: String,
    val preferenceJson: String,
    val currentLevel: String,
    val targetLevel: String,
    val progressJson: String
)

@Entity(tableName = "learning_memories")
data class LearningMemoryEntity(
    @PrimaryKey val id: String,
    val userId: String
)

@Entity(tableName = "memory_records", primaryKeys = ["memoryId", "cardId"])
data class MemoryRecordEntity(
    val memoryId: String,
    val cardId: String,
    val state: String,
    val retention: Double,
    val stability: Double,
    val confidence: Double,
    val reviewsJson: String
)

@Entity(tableName = "learning_roadmaps")
data class LearningRoadmapEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val goalJson: String,
    val status: String,
    val completedUnitIdsJson: String
)

@Entity(tableName = "learning_units", primaryKeys = ["roadmapId", "id"])
data class LearningUnitEntity(
    val roadmapId: String,
    val id: String,
    val title: String,
    val cardId: String,
    val parentId: String?,
    val durationMinutes: Int,
    val topicCode: String
)

@Entity(tableName = "learning_sessions")
data class LearningSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val completedUnitIdsJson: String
)

@Entity(tableName = "knowledge_packages")
data class KnowledgePackageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val version: String,
    val isCore: Boolean
)

@Entity(tableName = "knowledge_cards")
data class KnowledgeCardEntity(
    @PrimaryKey val id: String,
    val packageId: String,
    val title: String,
    val summary: String,
    val whyItMatters: String,
    val realWorldExample: String,
    val commonMistakes: String,
    val whenNotToUse: String,
    val bestPractice: String,
    val referencesJson: String,
    val officialDocumentationLink: String,
    val difficulty: String,
    val estimatedReadingTimeSeconds: Int,
    val careerImpact: String,
    val tagsJson: String,
    val prerequisiteCardIdsJson: String,
    val relatedTopicCodesJson: String
)
