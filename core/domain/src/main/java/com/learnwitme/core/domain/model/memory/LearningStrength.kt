package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningStrength
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Tracks topics that the learner has consistently
// demonstrated mastery over — backed by statistical evidence
// from review history and quiz performance.
//
// Strength is not self-reported. It is inferred deterministically
// by the Memory Engine from memory records across topic groups.
// =====================================================================

/**
 * Responsibility: Represents a single topic that the learner has
 * demonstrably mastered based on memory record analysis.
 *
 * A topic qualifies as a Strength when:
 * - Its average retention across all associated cards is ≥ 0.85
 * - Its mastery rate (cards in MASTERED state / total cards) is ≥ 0.80
 * - It has been stable for at least [MIN_STABILITY_SESSIONS] sessions
 *
 * Invariants:
 * - [topicCode] must be non-blank and follow the `tp_` prefix convention.
 * - [averageRetention] must be in range [0.0, 1.0].
 * - [masteryRate] must be in range [0.0, 1.0].
 * - [masteredCardCount] must be non-negative.
 * - [totalCardCount] must be positive.
 * - [masteredCardCount] must not exceed [totalCardCount].
 * - [detectedEpochMs] must be positive.
 *
 * @param topicCode          Domain-level topic code (e.g., "tp_networking").
 * @param topicName          Human-readable topic label.
 * @param averageRetention   Mean retention score across all cards in the topic.
 * @param masteryRate        Ratio of MASTERED cards to total cards in the topic.
 * @param masteredCardCount  Absolute count of cards in MASTERED state.
 * @param totalCardCount     Total card count for this topic in the learner's memory.
 * @param detectedEpochMs    Epoch timestamp (ms) when this strength was first detected.
 * @param confirmedEpochMs   Epoch timestamp (ms) of last re-confirmation (null if not re-confirmed yet).
 */
@Serializable
data class LearningStrength(
    val topicCode: String,
    val topicName: String,
    val averageRetention: Double,
    val masteryRate: Double,
    val masteredCardCount: Int,
    val totalCardCount: Int,
    val detectedEpochMs: Long,
    val confirmedEpochMs: Long? = null
) : ValueObject {

    init {
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
        require(topicCode.startsWith("tp_")) { "Topic code must start with 'tp_' (got '$topicCode')" }
        require(topicName.isNotBlank()) { "Topic name cannot be blank" }
        require(averageRetention in 0.0..1.0) {
            "Average retention must be in range [0.0, 1.0] (got $averageRetention)"
        }
        require(masteryRate in 0.0..1.0) {
            "Mastery rate must be in range [0.0, 1.0] (got $masteryRate)"
        }
        require(masteredCardCount >= 0) {
            "Mastered card count cannot be negative (got $masteredCardCount)"
        }
        require(totalCardCount > 0) {
            "Total card count must be positive (got $totalCardCount)"
        }
        require(masteredCardCount <= totalCardCount) {
            "Mastered card count ($masteredCardCount) cannot exceed total card count ($totalCardCount)"
        }
        require(detectedEpochMs > 0) {
            "Detected timestamp must be a positive epoch value (got $detectedEpochMs)"
        }
        if (confirmedEpochMs != null) {
            require(confirmedEpochMs > 0) {
                "Confirmed timestamp must be a positive epoch value (got $confirmedEpochMs)"
            }
            require(confirmedEpochMs >= detectedEpochMs) {
                "Confirmed timestamp cannot be before detected timestamp"
            }
        }
    }

    /** Returns true if this strength has been re-confirmed after initial detection. */
    val isConfirmed: Boolean get() = confirmedEpochMs != null

    companion object {
        /** Minimum retention threshold to qualify as a Strength. */
        const val MIN_RETENTION_THRESHOLD: Double = 0.85

        /** Minimum mastery rate to qualify as a Strength. */
        const val MIN_MASTERY_RATE: Double = 0.80

        /** Minimum session count required to produce a reliable strength signal. */
        const val MIN_STABILITY_SESSIONS: Int = 5
    }
}
