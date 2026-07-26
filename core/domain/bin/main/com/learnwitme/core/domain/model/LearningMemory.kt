package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable

enum class MemoryState {
    NEW, LEARNING, REVIEWING, REMEMBERED, MASTERED, FORGOTTEN, RECOVERED
}

/**
 * Responsibility: Represents the implicit and explicit signals of a review performance.
 */
@Serializable
data class ReviewQuality(
    val accuracy: Double,
    val hintUsed: Boolean,
    val responseTimeMs: Long,
    val confidenceRating: Double
) : ValueObject {
    init {
        require(accuracy in 0.0..1.0) { "Accuracy must be in 0.0..1.0 (got $accuracy)" }
        require(responseTimeMs >= 0) { "Response time cannot be negative" }
        require(confidenceRating in 0.0..1.0) { "Confidence rating must be in 0.0..1.0 (got $confidenceRating)" }
    }
}

/**
 * Responsibility: Represents a single record of review session metrics for active recall.
 */
@Serializable
data class MemoryReview(
    val timestamp: Long,
    val accuracy: Double, // Range: 0.0..1.0
    val hintUsed: Boolean,
    val confidenceRating: Double // Range: 0.0..1.0
) : ValueObject {
    init {
        require(timestamp > 0) { "Review timestamp must be positive" }
        require(accuracy in 0.0..1.0) { "Review accuracy must be between 0.0 and 1.0 (got $accuracy)" }
        require(confidenceRating in 0.0..1.0) { "Confidence rating must be between 0.0 and 1.0 (got $confidenceRating)" }
    }
}

/**
 * Responsibility: Holds the cognitive memory metrics and history for a specific KnowledgeCard.
 */
@Serializable
data class MemoryRecord(
    val cardId: KnowledgeCardId,
    val state: MemoryState,
    val retention: Double, // Range: 0.0..1.0
    val stability: Double, // Non-negative factor
    val confidence: Double, // Range: 0.0..1.0
    val reviews: List<MemoryReview> = emptyList()
) : ValueObject {

    init {
        require(retention in 0.0..1.0) { "Retention must be between 0.0 and 1.0 (got $retention)" }
        require(confidence in 0.0..1.0) { "Confidence must be between 0.0 and 1.0 (got $confidence)" }
        require(stability >= 0.0) { "Stability factor must be non-negative (got $stability)" }

        // Validate chronological history
        if (reviews.size > 1) {
            for (i in 0 until reviews.size - 1) {
                require(reviews[i].timestamp <= reviews[i + 1].timestamp) {
                    "Review history must be sorted chronologically"
                }
            }
        }
    }

    /**
     * Determines whether the transition from current state to the newState is valid.
     */
    fun isValidTransition(to: MemoryState): Boolean {
        if (state == to) return true
        return when (state) {
            MemoryState.NEW -> to == MemoryState.LEARNING
            MemoryState.LEARNING -> to == MemoryState.REVIEWING || to == MemoryState.FORGOTTEN
            MemoryState.REVIEWING -> to == MemoryState.REMEMBERED || to == MemoryState.MASTERED || to == MemoryState.FORGOTTEN
            MemoryState.REMEMBERED -> to == MemoryState.REVIEWING || to == MemoryState.MASTERED || to == MemoryState.FORGOTTEN
            MemoryState.MASTERED -> to == MemoryState.FORGOTTEN
            MemoryState.FORGOTTEN -> to == MemoryState.RECOVERED || to == MemoryState.LEARNING
            MemoryState.RECOVERED -> to == MemoryState.REMEMBERED || to == MemoryState.MASTERED || to == MemoryState.FORGOTTEN
        }
    }

    /**
     * Safely transitions to a new state after validating transition invariants.
     */
    fun transitionTo(newState: MemoryState): MemoryRecord {
        require(isValidTransition(newState)) {
            "Illegal memory state transition from $state to $newState"
        }
        return this.copy(state = newState)
    }

    /**
     * Computes the new record after applying a review result.
     */
    fun applyReview(review: MemoryReview): MemoryRecord {
        val newReviews = (reviews + review).sortedBy { it.timestamp }
        val isCorrect = review.accuracy >= 0.6

        val nextState = when (state) {
            MemoryState.NEW -> MemoryState.LEARNING
            MemoryState.LEARNING -> if (isCorrect) MemoryState.REVIEWING else MemoryState.FORGOTTEN
            MemoryState.REVIEWING -> if (isCorrect) {
                if (newReviews.size >= 3) MemoryState.REMEMBERED else MemoryState.REVIEWING
            } else {
                MemoryState.FORGOTTEN
            }
            MemoryState.REMEMBERED -> if (isCorrect) {
                if (newReviews.size >= 5) MemoryState.MASTERED else MemoryState.REMEMBERED
            } else {
                MemoryState.FORGOTTEN
            }
            MemoryState.MASTERED -> if (isCorrect) MemoryState.MASTERED else MemoryState.FORGOTTEN
            MemoryState.FORGOTTEN -> if (isCorrect) MemoryState.RECOVERED else MemoryState.FORGOTTEN
            MemoryState.RECOVERED -> if (isCorrect) MemoryState.REMEMBERED else MemoryState.FORGOTTEN
        }

        val nextStability = if (isCorrect) {
            stability * (1.0 + review.accuracy)
        } else {
            stability * 0.5
        }

        return this.copy(
            state = nextState,
            retention = review.accuracy,
            stability = nextStability,
            confidence = review.confidenceRating,
            reviews = newReviews
        )
    }
}

/**
 * Responsibility: Serves as the Aggregate Root that represents and manages
 * the user's cognitive memory states, retention scores, stability factor,
 * and review histories across various KnowledgeCards.
 */
data class LearningMemory(
    override val id: LearningMemoryId,
    val userId: UserId,
    val records: List<MemoryRecord> = emptyList()
) : AggregateRoot<LearningMemoryId> {

    init {
        // Invariant: No duplicate records for the same card
        val cardIds = records.map { it.cardId }
        require(cardIds.size == cardIds.toSet().size) {
            "LearningMemory cannot contain duplicate records for the same KnowledgeCard"
        }
    }

    /**
     * Finds the record for a specific KnowledgeCard.
     */
    fun findRecord(cardId: KnowledgeCardId): MemoryRecord? {
        return records.find { it.cardId == cardId }
    }

    /**
     * Records a review session for a KnowledgeCard, updating its memory state.
     */
    fun recordReview(cardId: KnowledgeCardId, review: MemoryReview): LearningMemory {
        val existingRecord = findRecord(cardId) ?: MemoryRecord(
            cardId = cardId,
            state = MemoryState.NEW,
            retention = 1.0,
            stability = 1.0,
            confidence = review.confidenceRating,
            reviews = emptyList()
        )

        val updatedRecord = existingRecord.applyReview(review)
        return updateRecord(updatedRecord)
    }

    /**
     * Marks a card as forgotten (due to time decay or quiz failures).
     */
    fun markForgotten(cardId: KnowledgeCardId): LearningMemory {
        val record = findRecord(cardId) ?: return this
        val updatedRecord = record.transitionTo(MemoryState.FORGOTTEN).copy(retention = 0.0)
        return updateRecord(updatedRecord)
    }

    /**
     * Recovers a forgotten card via remedial review.
     */
    fun recover(cardId: KnowledgeCardId, review: MemoryReview): LearningMemory {
        val record = findRecord(cardId) ?: return this
        require(record.state == MemoryState.FORGOTTEN) {
            "Only FORGOTTEN records can be recovered (current: ${record.state})"
        }

        val updatedRecord = record.applyReview(review)

        return updateRecord(updatedRecord)
    }

    /**
     * Increases the confidence level of a specific card's memory record.
     */
    fun increaseConfidence(cardId: KnowledgeCardId, amount: Double): LearningMemory {
        val record = findRecord(cardId) ?: return this
        val newConfidence = (record.confidence + amount).coerceIn(0.0, 1.0)
        val updatedRecord = record.copy(confidence = newConfidence)
        return updateRecord(updatedRecord)
    }

    /**
     * Decreases the confidence level of a specific card's memory record.
     */
    fun decreaseConfidence(cardId: KnowledgeCardId, amount: Double): LearningMemory {
        val record = findRecord(cardId) ?: return this
        val newConfidence = (record.confidence - amount).coerceIn(0.0, 1.0)
        val updatedRecord = record.copy(confidence = newConfidence)
        return updateRecord(updatedRecord)
    }

    /**
     * Updates the calculated retention of a card directly.
     */
    fun updateRetention(cardId: KnowledgeCardId, newRetention: Double): LearningMemory {
        val record = findRecord(cardId) ?: return this
        val updatedRecord = record.copy(retention = newRetention.coerceIn(0.0, 1.0))
        return updateRecord(updatedRecord)
    }

    /**
     * Detects weak topics by analyzing the memory states of cards belonging to specific topics.
     * Returns topic codes that are considered "weak" (e.g. have forgotten cards or low average retention).
     */
    fun detectWeakTopics(cardToTopicMapper: (KnowledgeCardId) -> String): Set<String> {
        val weakTopics = mutableSetOf<String>()
        val topicGroups = records.groupBy { cardToTopicMapper(it.cardId) }

        for ((topicCode, recordsInTopic) in topicGroups) {
            val hasForgotten = recordsInTopic.any { it.state == MemoryState.FORGOTTEN }
            val avgRetention = recordsInTopic.map { it.retention }.average()
            if (hasForgotten || avgRetention < 0.7) {
                weakTopics.add(topicCode)
            }
        }
        return weakTopics
    }

    private fun updateRecord(updatedRecord: MemoryRecord): LearningMemory {
        val newRecords = records.filter { it.cardId != updatedRecord.cardId } + updatedRecord
        return this.copy(records = newRecords)
    }
}

data class MemoryDecision(
    val updatedMemory: LearningMemory,
    val updatedRecord: MemoryRecord,
    val stateTransition: Pair<MemoryState, MemoryState>,
    val isRetentionImproved: Boolean,
    val isWeakTopicDetected: Boolean
) : ValueObject
