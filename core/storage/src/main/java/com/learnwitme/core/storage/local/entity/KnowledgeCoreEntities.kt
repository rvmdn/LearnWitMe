package com.learnwitme.core.storage.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistence entity mapping to the UserKnowledge Aggregate Root.
 * Stores user skill competencies, active learning cards, and mastered cards.
 */
@Entity(tableName = "user_knowledge")
data class UserKnowledgeEntity(
    @PrimaryKey val userId: String,
    val competenciesJson: String,
    val activeCardIdsJson: String,
    val masteredCardIdsJson: String
)

/**
 * Persistence entity mapping to the ReviewSchedule Entity within the Learner Bounded Context.
 * Optimized with composite primary key (userId, cardId) and indices for due review queries.
 */
@Entity(
    tableName = "review_schedules",
    primaryKeys = ["userId", "cardId"],
    indices = [Index(value = ["userId"]), Index(value = ["nextReviewTimestamp"])]
)
data class ReviewScheduleEntity(
    val userId: String,
    val cardId: String,
    val intervalDays: Int,
    val nextReviewTimestamp: Long,
    val easeFactor: Double,
    val repetitions: Int
)

/**
 * Persistence entity mapping to Recommendation Value Object / Entity.
 * Stores personalized daily feed recommendations per user.
 */
@Entity(
    tableName = "recommendations",
    primaryKeys = ["userId", "cardId"],
    indices = [Index(value = ["userId"])]
)
data class RecommendationEntity(
    val userId: String,
    val cardId: String,
    val score: Double,
    val reason: String
)
