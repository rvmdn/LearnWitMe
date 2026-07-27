package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningWeakness
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Tracks topics where the learner has demonstrated
// consistent difficulty — repeated failures, high hint usage,
// or persistently low retention. Used by Recommendation Engine
// and Quiz Engine to prioritize remedial content.
//
// Weakness is not self-reported. It is inferred deterministically
// by the Memory Engine from failure patterns in memory records.
// =====================================================================

/**
 * Classifies the severity of a detected learning weakness.
 */
enum class WeaknessSeverity {
    /** Mild — retention is declining but above critical threshold. */
    MILD,

    /** Moderate — retention is at or near the critical threshold. */
    MODERATE,

    /** Severe — repeated failures; topic is frequently in FORGOTTEN state. */
    SEVERE
}

/**
 * Responsibility: Represents a single topic where the learner has
 * demonstrably struggled based on memory record analysis.
 *
 * A topic qualifies as a Weakness when:
 * - Its average retention across all associated cards is ≤ 0.60
 * - OR its failure rate (cards in FORGOTTEN state / total cards) is ≥ 0.30
 * - OR the learner has failed ≥ 3 consecutive reviews on any card in this topic
 *
 * Invariants:
 * - [topicCode] must be non-blank and follow the `tp_` prefix convention.
 * - [averageRetention] must be in range [0.0, 1.0].
 * - [failureRate] must be in range [0.0, 1.0].
 * - [forgottenCardCount] must be non-negative.
 * - [totalCardCount] must be positive.
 * - [forgottenCardCount] must not exceed [totalCardCount].
 * - [consecutiveFailureCount] must be non-negative.
 * - [detectedEpochMs] must be positive.
 *
 * @param topicCode              Domain-level topic code (e.g., "tp_networking").
 * @param topicName              Human-readable topic label.
 * @param averageRetention       Mean retention score across all cards in the topic.
 * @param failureRate            Ratio of FORGOTTEN cards to total cards.
 * @param forgottenCardCount     Absolute count of cards in FORGOTTEN/RECOVERING state.
 * @param totalCardCount         Total card count for this topic in the learner's memory.
 * @param consecutiveFailureCount Max consecutive review failures across any single card in topic.
 * @param severity               Computed severity classification of this weakness.
 * @param detectedEpochMs        Epoch timestamp (ms) when this weakness was first detected.
 * @param lastReinforcedEpochMs  Epoch timestamp (ms) of last successful remedial review (null if never remediated).
 */
@Serializable
data class LearningWeakness(
    val topicCode: String,
    val topicName: String,
    val averageRetention: Double,
    val failureRate: Double,
    val forgottenCardCount: Int,
    val totalCardCount: Int,
    val consecutiveFailureCount: Int,
    val severity: WeaknessSeverity,
    val detectedEpochMs: Long,
    val lastReinforcedEpochMs: Long? = null
) : ValueObject {

    init {
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
        require(topicCode.startsWith("tp_")) { "Topic code must start with 'tp_' (got '$topicCode')" }
        require(topicName.isNotBlank()) { "Topic name cannot be blank" }
        require(averageRetention in 0.0..1.0) {
            "Average retention must be in range [0.0, 1.0] (got $averageRetention)"
        }
        require(failureRate in 0.0..1.0) {
            "Failure rate must be in range [0.0, 1.0] (got $failureRate)"
        }
        require(forgottenCardCount >= 0) {
            "Forgotten card count cannot be negative (got $forgottenCardCount)"
        }
        require(totalCardCount > 0) {
            "Total card count must be positive (got $totalCardCount)"
        }
        require(forgottenCardCount <= totalCardCount) {
            "Forgotten card count ($forgottenCardCount) cannot exceed total card count ($totalCardCount)"
        }
        require(consecutiveFailureCount >= 0) {
            "Consecutive failure count cannot be negative (got $consecutiveFailureCount)"
        }
        require(detectedEpochMs > 0) {
            "Detected timestamp must be a positive epoch value (got $detectedEpochMs)"
        }
        if (lastReinforcedEpochMs != null) {
            require(lastReinforcedEpochMs > 0) {
                "Last reinforced timestamp must be positive (got $lastReinforcedEpochMs)"
            }
        }
    }

    /** Returns true if this weakness has ever been successfully remediated. */
    val hasBeenRemediated: Boolean get() = lastReinforcedEpochMs != null

    /** Returns true if the weakness is at a critical level requiring urgent attention. */
    val isCritical: Boolean get() = severity == WeaknessSeverity.SEVERE

    companion object {
        /** Maximum retention threshold to qualify as a Weakness. */
        const val MAX_RETENTION_THRESHOLD: Double = 0.60

        /** Minimum failure rate to qualify as a Weakness. */
        const val MIN_FAILURE_RATE_THRESHOLD: Double = 0.30

        /** Consecutive failure count threshold for SEVERE classification. */
        const val SEVERE_CONSECUTIVE_THRESHOLD: Int = 3
    }
}
