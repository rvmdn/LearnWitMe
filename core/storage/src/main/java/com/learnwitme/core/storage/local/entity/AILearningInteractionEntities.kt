package com.learnwitme.core.storage.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row for each immutable AI learning interaction. Its interaction ID is the parent key used by
 * the optional feedback, outcome, and memory-impact records.
 */
@Entity(
    tableName = "ai_learning_interactions",
    indices = [Index(value = ["userId"]), Index(value = ["startedAt"])]
)
data class AILearningInteractionEntity(
    @PrimaryKey val interactionId: String,
    val userId: String,
    val action: String,
    val cardId: String?,
    val topicCode: String?,
    val topicName: String?,
    val topicDescription: String?,
    val startedAt: Long,
    val completedAt: Long?,
    val provider: String?,
    val model: String?,
    val promptTokens: Int,
    val completionTokens: Int,
    val personalizationPreset: String?
)

/**
 * Optional learner feedback. The shared primary key represents its one-to-one association with an
 * AI learning interaction; the existing schema convention does not use Room foreign keys.
 */
@Entity(tableName = "ai_learning_feedback")
data class AILearningFeedbackEntity(
    @PrimaryKey val interactionId: String,
    val usefulnessRating: Int,
    val confidenceSignal: Double?,
    val comment: String?,
    val createdAt: Long
)

/** Optional outcome for an AI learning interaction. */
@Entity(tableName = "ai_learning_outcomes")
data class AILearningOutcomeEntity(
    @PrimaryKey val interactionId: String,
    val status: String,
    val summary: String?,
    val completedAt: Long,
    val metadataJson: String
)

/**
 * Deterministic impact evidence. This is not a LearningMemory persistence model and cannot update
 * the LearningMemory aggregate.
 */
@Entity(tableName = "ai_learning_memory_impacts")
data class AILearningMemoryImpactEntity(
    @PrimaryKey val interactionId: String,
    val cardId: String?,
    val topicCode: String?,
    val topicName: String?,
    val topicDescription: String?,
    val action: String,
    val outcomeStatus: String?,
    val usefulnessRating: Int,
    val confidenceSignal: Double?,
    val classification: String,
    val confidenceDelta: Double,
    val calculatedAt: Long
)
