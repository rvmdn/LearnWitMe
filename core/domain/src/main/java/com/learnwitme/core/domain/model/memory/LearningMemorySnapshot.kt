package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningMemorySnapshot
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Immutable, point-in-time snapshot of a learner's
// entire LearningMemory state. Used for historical analysis,
// long-term trend tracking, and regression detection.
//
// A snapshot is created by the Memory Engine at scheduled intervals
// or after significant state transitions (e.g., weekly digest,
// before/after intensive study periods).
//
// Immutability guarantee: All fields are val. No mutation allowed.
// The snapshot captures exactly what the memory state was at a
// given moment in time — it cannot be updated after creation.
// =====================================================================

/**
 * Responsibility: An immutable, timestamped capture of the full
 * Learning Memory state for one learner at a specific moment.
 *
 * Snapshots enable:
 * - Historical trend analysis (e.g., "How has retention changed this month?")
 * - Regression detection (e.g., "3 new weaknesses emerged this week")
 * - Audit trails for AI enhancement components
 * - Comparison between time periods without live data mutation
 *
 * Invariants:
 * - [snapshotId] must be non-blank.
 * - [memoryId] references the originating LearningMemory aggregate.
 * - [userId] must match the LearningMemory it was taken from.
 * - [capturedEpochMs] must be a positive epoch value.
 * - [totalCardsTracked] must be non-negative.
 * - [strengthCount] + [weaknessCount] must not exceed [totalCardsTracked].
 * - [overallRetentionAverage] must be in range [0.0, 1.0].
 *
 * @param snapshotId              Unique identifier for this snapshot record.
 * @param memoryId                References the LearningMemory this was captured from.
 * @param userId                  Owner of the LearningMemory at time of capture.
 * @param capturedEpochMs         Epoch timestamp (ms) when this snapshot was taken.
 * @param preference              Learner's inferred preference at time of snapshot.
 * @param pattern                 Learner's behavioral pattern at time of snapshot.
 * @param strengths               List of strengths detected at time of snapshot.
 * @param weaknesses              List of weaknesses detected at time of snapshot.
 * @param behaviors               List of behaviors detected at time of snapshot.
 * @param totalCardsTracked       Total memory records in the LearningMemory at snapshot time.
 * @param overallRetentionAverage Mean retention score across all records at snapshot time.
 * @param snapshotReason          Human-readable reason this snapshot was taken (e.g., "Weekly digest").
 */
@Serializable
data class LearningMemorySnapshot(
    val snapshotId: String,
    val memoryId: LearningMemoryId,
    val userId: UserId,
    val capturedEpochMs: Long,
    val preference: LearningPreference,
    val pattern: LearningPattern,
    val strengths: List<LearningStrength>,
    val weaknesses: List<LearningWeakness>,
    val behaviors: List<LearningBehavior>,
    val totalCardsTracked: Int,
    val overallRetentionAverage: Double,
    val snapshotReason: String
) : ValueObject {

    init {
        require(snapshotId.isNotBlank()) { "Snapshot ID cannot be blank" }
        require(capturedEpochMs > 0) {
            "Captured timestamp must be a positive epoch value (got $capturedEpochMs)"
        }
        require(totalCardsTracked >= 0) {
            "Total cards tracked cannot be negative (got $totalCardsTracked)"
        }
        require(overallRetentionAverage in 0.0..1.0) {
            "Overall retention average must be in range [0.0, 1.0] (got $overallRetentionAverage)"
        }
        require(snapshotReason.isNotBlank()) { "Snapshot reason cannot be blank" }
    }

    /** Total count of identified strength topics at snapshot time. */
    val strengthCount: Int get() = strengths.size

    /** Total count of identified weakness topics at snapshot time. */
    val weaknessCount: Int get() = weaknesses.size

    /** Total count of observed behaviors at snapshot time. */
    val behaviorCount: Int get() = behaviors.size

    /** Returns true if the snapshot shows more weaknesses than strengths. */
    val hasMoreWeaknessesThanStrengths: Boolean get() = weaknessCount > strengthCount

    /** Returns true if any SEVERE weaknesses were present at snapshot time. */
    val hasSevereWeaknesses: Boolean get() = weaknesses.any { it.isCritical }
}
