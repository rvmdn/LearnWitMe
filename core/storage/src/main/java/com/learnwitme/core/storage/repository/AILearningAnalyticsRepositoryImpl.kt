package com.learnwitme.core.storage.repository

import com.learnwitme.core.analytics.AILearningAnalyticsEvent
import com.learnwitme.core.analytics.AILearningAnalyticsRepository
import com.learnwitme.core.analytics.QueuedAnalyticsEvent
import com.learnwitme.core.storage.local.dao.AILearningAnalyticsEventDao
import com.learnwitme.core.storage.mapper.AILearningAnalyticsStorageMapper

/**
 * Storage implementation of [AILearningAnalyticsRepository] backed by Room DAO ([AILearningAnalyticsEventDao]).
 *
 * Guarantees:
 * - Idempotent enqueue operations via primary key [AILearningAnalyticsEvent.eventId].
 * - Deterministic event mapping and retrieval.
 */
import javax.inject.Inject

class AILearningAnalyticsRepositoryImpl @Inject constructor(
    private val dao: AILearningAnalyticsEventDao
) : AILearningAnalyticsRepository {

    override suspend fun enqueue(events: List<AILearningAnalyticsEvent>) {
        if (events.isEmpty()) return
        val entities = events.map { event ->
            AILearningAnalyticsStorageMapper.toEntity(event)
        }
        dao.insertAll(entities)
    }

    override suspend fun getPendingEvents(limit: Int): List<QueuedAnalyticsEvent> {
        val entities = dao.getPendingEvents(limit)
        return entities.map { entity ->
            AILearningAnalyticsStorageMapper.toQueuedEvent(entity)
        }
    }

    override suspend fun markDelivered(eventIds: List<String>) {
        if (eventIds.isEmpty()) return
        dao.markDelivered(eventIds)
    }

    override suspend fun markFailed(eventId: String, errorMessage: String?, nextRetryAt: Long?) {
        val currentTime = System.currentTimeMillis()
        dao.markFailed(
            eventId = eventId,
            lastAttemptAt = currentTime,
            nextRetryAt = nextRetryAt,
            errorMessage = errorMessage
        )
    }

    override suspend fun getPendingCount(): Int {
        return dao.getPendingCount()
    }
}
