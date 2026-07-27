package com.learnwitme.core.domain.model

import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningMemorySnapshot
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness
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
 * the full accumulated learning knowledge of one learner, including:
 *
 * 1. Per-card cognitive memory states, retention scores, stability factors,
 *    and review histories ([records]) — from Sprint 1.
 *
 * 2. Inferred learning style preference ([preference]) — Sprint 9.
 * 3. Deterministic behavioral learning patterns ([pattern]) — Sprint 9.
 * 4. Detected topic mastery areas ([strengths]) — Sprint 9.
 * 5. Detected struggling topic areas ([weaknesses]) — Sprint 9.
 * 6. Observable recurring study behaviors ([behaviors]) — Sprint 9.
 *
 * The Memory Engine is the sole writer of this aggregate.
 * The Recommendation Engine, Quiz Engine, and AI Enhancement Layer
 * consume this aggregate as read-only input.
 *
 * Architecture Invariant: AI never writes to LearningMemory.
 * LearningMemory is deterministic domain data — not AI output.
 */
data class LearningMemory(
    override val id: LearningMemoryId,
    val userId: UserId,
    val records: List<MemoryRecord> = emptyList(),

    // ── Sprint 9: Learner Profile Memory Layer ────────────────────────
    /** Inferred cognitive learning style for this learner. */
    val preference: LearningPreference = LearningPreference.undetermined(nowEpochMs = 1L),

    /** Deterministic behavioral learning patterns derived from session analysis. */
    val pattern: LearningPattern = LearningPattern.empty(nowEpochMs = 1L),

    /** Topics where mastery has been deterministically detected. */
    val strengths: List<LearningStrength> = emptyList(),

    /** Topics where consistent difficulty has been deterministically detected. */
    val weaknesses: List<LearningWeakness> = emptyList(),

    /** Observable recurring study behaviors detected from session data. */
    val behaviors: List<LearningBehavior> = emptyList()
    // ─────────────────────────────────────────────────────────────────

) : AggregateRoot<LearningMemoryId> {

    init {
        // Invariant: No duplicate records for the same card
        val cardIds = records.map { it.cardId }
        require(cardIds.size == cardIds.toSet().size) {
            "LearningMemory cannot contain duplicate records for the same KnowledgeCard"
        }

        // Invariant: No duplicate strengths for the same topic
        val strengthTopics = strengths.map { it.topicCode }
        require(strengthTopics.size == strengthTopics.toSet().size) {
            "LearningMemory cannot contain duplicate strength entries for the same topic"
        }

        // Invariant: No duplicate weaknesses for the same topic
        val weaknessTopics = weaknesses.map { it.topicCode }
        require(weaknessTopics.size == weaknessTopics.toSet().size) {
            "LearningMemory cannot contain duplicate weakness entries for the same topic"
        }

        // Invariant: A topic cannot simultaneously be a Strength and a Weakness
        val strengthSet = strengthTopics.toSet()
        val weaknessSet = weaknessTopics.toSet()
        val overlap = strengthSet.intersect(weaknessSet)
        require(overlap.isEmpty()) {
            "A topic cannot be both a Strength and a Weakness simultaneously: $overlap"
        }

        // Invariant: No duplicate behavior types (one record per BehaviorType)
        val behaviorTypes = behaviors.map { it.type }
        require(behaviorTypes.size == behaviorTypes.toSet().size) {
            "LearningMemory cannot contain duplicate behavior entries for the same BehaviorType"
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

    // ── Sprint 9: Learner Profile Memory Operations ───────────────────

    /**
     * Returns the inferred learning preference for this learner.
     * Provided as an explicit accessor to make AI consumer contracts clear.
     */
    fun getLearningPreference(): LearningPreference = preference

    /**
     * Returns the deterministic behavioral learning pattern for this learner.
     */
    fun getLearningPattern(): LearningPattern = pattern

    /**
     * Updates the inferred learning style preference.
     * Called by the Memory Engine after analyzing sufficient behavioral signals.
     *
     * @param newPreference The newly computed preference from the Memory Engine.
     * @return A new immutable LearningMemory with the updated preference.
     */
    fun updatePreference(newPreference: LearningPreference): LearningMemory {
        return this.copy(preference = newPreference)
    }

    /**
     * Updates the deterministic behavioral learning pattern.
     * Called by the Memory Engine after each session analysis cycle.
     *
     * @param newPattern The newly computed pattern from the Memory Engine.
     * @return A new immutable LearningMemory with the updated pattern.
     */
    fun updatePattern(newPattern: LearningPattern): LearningMemory {
        return this.copy(pattern = newPattern)
    }

    /**
     * Records or updates a detected topic strength.
     * If a strength for the same topic already exists, it is replaced.
     *
     * Precondition: [strength.topicCode] must NOT exist in [weaknesses].
     *
     * @param strength The newly detected or updated strength.
     * @return A new immutable LearningMemory with the strength recorded.
     */
    fun recordStrength(strength: LearningStrength): LearningMemory {
        require(weaknesses.none { it.topicCode == strength.topicCode }) {
            "Cannot record a Strength for topic '${strength.topicCode}' — it is currently tracked as a Weakness. " +
                    "Resolve the Weakness before promoting to Strength."
        }
        val updatedStrengths = strengths.filter { it.topicCode != strength.topicCode } + strength
        return this.copy(strengths = updatedStrengths)
    }

    /**
     * Records or updates a detected topic weakness.
     * If a weakness for the same topic already exists, it is replaced.
     *
     * Precondition: [weakness.topicCode] must NOT exist in [strengths].
     *
     * @param weakness The newly detected or updated weakness.
     * @return A new immutable LearningMemory with the weakness recorded.
     */
    fun recordWeakness(weakness: LearningWeakness): LearningMemory {
        require(strengths.none { it.topicCode == weakness.topicCode }) {
            "Cannot record a Weakness for topic '${weakness.topicCode}' — it is currently tracked as a Strength. " +
                    "Remove the Strength entry before recording a Weakness."
        }
        val updatedWeaknesses = weaknesses.filter { it.topicCode != weakness.topicCode } + weakness
        return this.copy(weaknesses = updatedWeaknesses)
    }

    /**
     * Records or updates a detected observable behavior.
     * If a behavior of the same [BehaviorType] already exists, it is replaced.
     *
     * @param behavior The newly observed or updated behavior.
     * @return A new immutable LearningMemory with the behavior recorded.
     */
    fun recordBehavior(behavior: LearningBehavior): LearningMemory {
        val updatedBehaviors = behaviors.filter { it.type != behavior.type } + behavior
        return this.copy(behaviors = updatedBehaviors)
    }

    /**
     * Removes a weakness entry when a topic has been successfully remediated
     * and no longer qualifies as a weakness.
     *
     * If no weakness exists for the given topicCode, this is a no-op.
     *
     * @param topicCode The topic code of the resolved weakness.
     * @return A new immutable LearningMemory with the weakness removed.
     */
    fun removeResolvedWeakness(topicCode: String): LearningMemory {
        return this.copy(weaknesses = weaknesses.filter { it.topicCode != topicCode })
    }

    /**
     * Removes a strength entry when a topic has regressed below mastery thresholds.
     *
     * If no strength exists for the given topicCode, this is a no-op.
     *
     * @param topicCode The topic code of the regressed strength.
     * @return A new immutable LearningMemory with the strength removed.
     */
    fun removeRegressedStrength(topicCode: String): LearningMemory {
        return this.copy(strengths = strengths.filter { it.topicCode != topicCode })
    }

    /**
     * Computes the overall average retention score across all tracked memory records.
     * Returns 0.0 if no records exist.
     */
    fun computeOverallRetention(): Double {
        if (records.isEmpty()) return 0.0
        return records.map { it.retention }.average()
    }

    /**
     * Creates an immutable [LearningMemorySnapshot] capturing the full current state.
     * Called by the Memory Engine at scheduled intervals or after significant transitions.
     *
     * @param snapshotId    Unique identifier for the new snapshot.
     * @param capturedEpochMs The epoch timestamp at which the snapshot is being taken.
     * @param reason        Human-readable reason for this snapshot (e.g., "Weekly digest").
     * @return An immutable snapshot of the current LearningMemory state.
     */
    fun takeSnapshot(
        snapshotId: String,
        capturedEpochMs: Long,
        reason: String
    ): LearningMemorySnapshot {
        return LearningMemorySnapshot(
            snapshotId = snapshotId,
            memoryId = id,
            userId = userId,
            capturedEpochMs = capturedEpochMs,
            preference = preference,
            pattern = pattern,
            strengths = strengths.toList(),
            weaknesses = weaknesses.toList(),
            behaviors = behaviors.toList(),
            totalCardsTracked = records.size,
            overallRetentionAverage = computeOverallRetention(),
            snapshotReason = reason
        )
    }

    // ─────────────────────────────────────────────────────────────────

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
