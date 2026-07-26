package com.learnwitme.core.storage.local.entity

import androidx.room.Entity
import androidx.room.Index
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

@Entity(
    tableName = "learning_memories",
    indices = [Index(value = ["userId"])]
)
data class LearningMemoryEntity(
    @PrimaryKey val id: String,
    val userId: String
)

@Entity(
    tableName = "memory_records",
    primaryKeys = ["memoryId", "cardId"],
    indices = [Index(value = ["memoryId"])]
)
data class MemoryRecordEntity(
    val memoryId: String,
    val cardId: String,
    val state: String,
    val retention: Double,
    val stability: Double,
    val confidence: Double,
    val reviewsJson: String
)

@Entity(
    tableName = "learning_roadmaps",
    indices = [Index(value = ["userId"])]
)
data class LearningRoadmapEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val goalJson: String,
    val status: String,
    val completedUnitIdsJson: String
)

@Entity(
    tableName = "learning_units",
    primaryKeys = ["roadmapId", "id"],
    indices = [Index(value = ["roadmapId"])]
)
data class LearningUnitEntity(
    val roadmapId: String,
    val id: String,
    val title: String,
    val cardId: String,
    val parentId: String?,
    val durationMinutes: Int,
    val topicCode: String
)

@Entity(
    tableName = "learning_sessions",
    indices = [Index(value = ["userId"])]
)
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
    val checksum: String = "",
    val status: String = "DRAFT",
    val isCore: Boolean = false
)

@Entity(
    tableName = "knowledge_cards",
    indices = [Index(value = ["packageId"])]
)
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
    val skillsJson: String = "[]",
    val tagsJson: String,
    val prerequisiteCardIdsJson: String,
    val relatedTopicCodesJson: String
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetDifficulty: String
)

@Entity(
    tableName = "quiz_questions",
    primaryKeys = ["quizId", "id"],
    indices = [Index(value = ["quizId"])]
)
data class QuizQuestionEntity(
    val quizId: String,
    val id: String,
    val questionText: String,
    val correctOptionIndex: Int
)

@Entity(
    tableName = "quiz_options",
    primaryKeys = ["quizId", "questionId", "optionIndex"],
    indices = [Index(value = ["quizId", "questionId"])]
)
data class QuizOptionEntity(
    val quizId: String,
    val questionId: String,
    val optionIndex: Int,
    val text: String,
    val explanation: String
)

@Entity(
    tableName = "quiz_results",
    indices = [Index(value = ["userId"]), Index(value = ["quizId"])]
)
data class QuizResultEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val userId: String,
    val score: Double,
    val answersJson: String,
    val completedTimestamp: Long
)
