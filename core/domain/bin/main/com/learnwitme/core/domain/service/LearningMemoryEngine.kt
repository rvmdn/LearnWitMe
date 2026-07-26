package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.MemoryDecision
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryReview
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.ReviewQuality

/**
 * Responsibility: Stateless engine that calculates memory decay, updates retention/confidence,
 * processes state transitions, and detects weak topics.
 */
class LearningMemoryEngine {

    /**
     * Processes a review for a card, recalculates memory metrics, updates memory state,
     * and returns the memory decision.
     */
    fun processReview(
        memory: LearningMemory,
        cardId: KnowledgeCardId,
        quality: ReviewQuality,
        clock: Clock,
        cardToTopicMapper: (KnowledgeCardId) -> String
    ): MemoryDecision {
        val oldRecord = memory.findRecord(cardId)
        val oldState = oldRecord?.state ?: MemoryState.NEW

        // Translate ReviewQuality to MemoryReview
        val review = MemoryReview(
            timestamp = clock.currentTimeMillis(),
            accuracy = quality.accuracy,
            hintUsed = quality.hintUsed,
            confidenceRating = quality.confidenceRating
        )

        // Process review on the aggregate root
        val nextMemory = if (oldState == MemoryState.FORGOTTEN) {
            memory.recover(cardId, review)
        } else {
            memory.recordReview(cardId, review)
        }

        val newRecord = nextMemory.findRecord(cardId)
            ?: throw IllegalStateException("Record must exist after review processing")

        val newState = newRecord.state

        val isRetentionImproved = (oldRecord?.retention ?: 0.0) < newRecord.retention

        // Check if the topic of the current card is weak after the review
        val weakTopics = nextMemory.detectWeakTopics(cardToTopicMapper)
        val currentTopic = cardToTopicMapper(cardId)
        val isWeakTopicDetected = weakTopics.contains(currentTopic)

        return MemoryDecision(
            updatedMemory = nextMemory,
            updatedRecord = newRecord,
            stateTransition = Pair(oldState, newState),
            isRetentionImproved = isRetentionImproved,
            isWeakTopicDetected = isWeakTopicDetected
        )
    }

    /**
     * Calculates time-decay for a card's retention score.
     * If retention falls below the critical threshold (e.g. 0.6), transitions the card to FORGOTTEN.
     */
    fun decayMemory(
        memory: LearningMemory,
        cardId: KnowledgeCardId,
        clock: Clock
    ): LearningMemory {
        val record = memory.findRecord(cardId) ?: return memory
        if (record.state == MemoryState.NEW || record.state == MemoryState.FORGOTTEN) {
            return memory
        }

        // Calculate time elapsed since last review
        val lastReviewTime = record.reviews.lastOrNull()?.timestamp ?: clock.currentTimeMillis()
        val timeElapsedSeconds = (clock.currentTimeMillis() - lastReviewTime) / 1000.0

        if (timeElapsedSeconds <= 0.0) return memory

        // Simple deterministic decay model: retention = e ^ (-t / stability)
        // stability is scaled to hours for cognitive realism
        val newRetention = kotlin.math.exp(-timeElapsedSeconds / (record.stability * 3600.0))

        return if (newRetention < 0.6) {
            memory.markForgotten(cardId)
        } else {
            memory.updateRetention(cardId, newRetention)
        }
    }
}
