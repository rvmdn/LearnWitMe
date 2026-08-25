package com.learnwitme.core.storage.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for persistent storage of AI learning analytics events.
 *
 * Privacy Guarantees:
 * - Excludes raw prompts, free-text comments, and security credentials.
 * - Stores structured correlation IDs and sanitized metrics only.
 */
@Entity(
    tableName = "ai_learning_analytics_events",
    indices = [
        Index(value = ["interactionId"]),
        Index(value = ["deliveryStatus"]),
        Index(value = ["userId"])
    ]
)
data class AILearningAnalyticsEventEntity(
    @PrimaryKey
    val eventId: String,
    val eventType: String,
    val interactionId: String,
    val userId: String,
    val action: String,
    val timestamp: Long,
    val cardId: String?,
    val topicCode: String?,
    val provider: String?,
    val model: String?,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val personalizationPreset: String?,
    val outcomeStatus: String?,
    val summary: String?,
    val usefulnessRating: Int?,
    val confidenceSignal: Double?,
    val hasComment: Boolean,
    val classification: String?,
    val confidenceDelta: Double?,
    val deliveryStatus: String,
    val attemptCount: Int,
    val lastAttemptAt: Long?,
    val nextRetryAt: Long?,
    val errorMessage: String?
)
