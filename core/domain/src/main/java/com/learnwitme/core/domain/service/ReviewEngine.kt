package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.ReviewDecision
import com.learnwitme.core.domain.model.ReviewPolicy
import com.learnwitme.core.domain.model.ReviewPriority
import com.learnwitme.core.domain.model.ReviewQueue

/**
 * Responsibility: Stateless engine that calculates ulasan schedules, priority, overdue status,
 * and sorts the review queue.
 */
class ReviewEngine {

    /**
     * Calculates the review decision for a single MemoryRecord.
     */
    fun calculateDecision(
        record: MemoryRecord,
        clock: Clock,
        policy: ReviewPolicy
    ): ReviewDecision {
        val lastReviewTime = record.reviews.lastOrNull()?.timestamp ?: clock.currentTimeMillis()

        // 1. Calculate next review interval in seconds
        // Base interval is scaled by stability.
        // If confidence is low, interval is reduced.
        val baseIntervalSeconds = record.stability * 3600.0 // 1 stability = 1 hour
        val confidenceFactor = 0.5 + 0.5 * record.confidence // Range: 0.5..1.0
        val targetIntervalSeconds = (baseIntervalSeconds * confidenceFactor)
            .coerceIn(policy.minIntervalSeconds.toDouble(), policy.maxIntervalSeconds.toDouble())
            .toLong()

        val nextReviewTime = lastReviewTime + (targetIntervalSeconds * 1000L)

        // 2. Check overdue status
        val isOverdue = clock.currentTimeMillis() >= nextReviewTime

        // 3. Determine priority
        // Critical if overdue and retention is below threshold, or if forgotten
        val priority = when {
            record.state == MemoryState.FORGOTTEN -> ReviewPriority.CRITICAL
            isOverdue && record.retention < policy.criticalRetentionThreshold -> ReviewPriority.CRITICAL
            isOverdue -> ReviewPriority.HIGH
            record.retention < policy.criticalRetentionThreshold -> ReviewPriority.MEDIUM
            else -> ReviewPriority.LOW
        }

        // 4. Can be deferred?
        // Can be deferred only if priority is MEDIUM or LOW and it is not overdue
        val canBeDeferred = !isOverdue && (priority == ReviewPriority.MEDIUM || priority == ReviewPriority.LOW)

        val reason = when (priority) {
            ReviewPriority.CRITICAL -> if (record.state == MemoryState.FORGOTTEN) {
                "Card is FORGOTTEN and requires immediate remedial review"
            } else {
                "Retention (${record.retention}) is below critical threshold (${policy.criticalRetentionThreshold})"
            }
            ReviewPriority.HIGH -> "Review is overdue"
            ReviewPriority.MEDIUM -> "Retention is low, but review is not yet overdue"
            ReviewPriority.LOW -> "Memory is stable"
        }

        return ReviewDecision(
            cardId = record.cardId,
            nextReviewTime = nextReviewTime,
            priority = priority,
            isOverdue = isOverdue,
            canBeDeferred = canBeDeferred,
            reason = reason
        )
    }

    /**
     * Determines the prioritized review queue for a collection of card memory records.
     */
    fun determineQueue(
        memory: LearningMemory,
        clock: Clock,
        policy: ReviewPolicy
    ): ReviewQueue {
        val decisions = memory.records
            .filter { it.state != MemoryState.NEW } // Do not queue NEW cards for review
            .map { calculateDecision(it, clock, policy) }
            .sortedWith(
                compareBy<ReviewDecision> { it.priority.ordinal }
                    .thenByDescending { it.isOverdue }
                    .thenBy { it.nextReviewTime }
            )

        return ReviewQueue(decisions)
    }
}
