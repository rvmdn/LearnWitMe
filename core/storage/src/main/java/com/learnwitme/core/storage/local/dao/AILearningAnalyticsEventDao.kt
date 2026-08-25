package com.learnwitme.core.storage.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.learnwitme.core.storage.local.entity.AILearningAnalyticsEventEntity

@Dao
interface AILearningAnalyticsEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<AILearningAnalyticsEventEntity>)

    @Query("SELECT * FROM ai_learning_analytics_events WHERE deliveryStatus = 'PENDING' OR deliveryStatus = 'FAILED' ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getPendingEvents(limit: Int): List<AILearningAnalyticsEventEntity>

    @Query("UPDATE ai_learning_analytics_events SET deliveryStatus = 'DELIVERED' WHERE eventId IN (:eventIds)")
    suspend fun markDelivered(eventIds: List<String>)

    @Query("UPDATE ai_learning_analytics_events SET deliveryStatus = 'FAILED', attemptCount = attemptCount + 1, lastAttemptAt = :lastAttemptAt, nextRetryAt = :nextRetryAt, errorMessage = :errorMessage WHERE eventId = :eventId")
    suspend fun markFailed(eventId: String, lastAttemptAt: Long, nextRetryAt: Long?, errorMessage: String?)

    @Query("SELECT COUNT(*) FROM ai_learning_analytics_events WHERE deliveryStatus = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT * FROM ai_learning_analytics_events WHERE eventId = :eventId")
    suspend fun getEventById(eventId: String): AILearningAnalyticsEventEntity?
}
