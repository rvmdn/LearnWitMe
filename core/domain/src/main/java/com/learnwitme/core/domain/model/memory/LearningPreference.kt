package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningPreference
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Represents a learner's preferred cognitive approach
// to acquiring new knowledge. This is a memory-domain concept distinct
// from UserProfile preferences (topic list + daily limit).
//
// Note: The existing `LearningPreference` in ValueObjects.kt belongs
// to the UserProfile aggregate (preferred topics + daily limit minutes).
// This `LearningPreference` belongs exclusively to the LearningMemory
// aggregate and describes the cognitive/pedagogical learning style.
//
// Architecture Note: Memory Engine owns this. Not UserProfile Engine.
// =====================================================================

/**
 * Enumerates the supported pedagogical learning styles.
 *
 * These values are determined by observing how the learner interacts
 * with the platform over time, NOT by user self-declaration.
 * They are updated deterministically by the Memory Engine.
 */
enum class LearningStyleType {

    /**
     * Learner absorbs information better through diagrams,
     * structured layouts, and visual comparisons.
     */
    VISUAL,

    /**
     * Learner prefers hands-on exercises, code examples,
     * and real-world scenarios before reading theory.
     */
    PRACTICAL,

    /**
     * Learner needs to understand the foundational theory
     * and "why" before attempting practical exercises.
     */
    THEORY_FIRST,

    /**
     * Learner understands concepts most effectively when
     * concrete examples are presented before formal definitions.
     */
    EXAMPLE_FIRST,

    /**
     * Learner style has not been determined yet.
     * This is the initial state for every new learner.
     */
    UNDETERMINED
}

/**
 * Responsibility: Represents the deterministically-inferred
 * cognitive learning style for a specific learner within the
 * Learning Memory domain.
 *
 * Learning style is updated by the Memory Engine based on
 * observed interaction signals — not manually configured.
 *
 * Invariants:
 * - [confidenceScore] must be in range [0.0, 1.0].
 * - A style of [LearningStyleType.UNDETERMINED] must have a
 *   confidence score of 0.0.
 * - [observationCount] must be non-negative.
 * - [lastUpdatedEpochMs] must be a positive timestamp.
 *
 * @param primaryStyle   The dominant learning style inferred from behavior.
 * @param secondaryStyle An optional secondary style when signals are mixed.
 * @param confidenceScore Confidence level in the primary style inference (0.0–1.0).
 * @param observationCount Number of behavioral signals used to infer this preference.
 * @param lastUpdatedEpochMs Epoch timestamp (ms) of the last style recalculation.
 */
@Serializable
data class LearningPreference(
    val primaryStyle: LearningStyleType,
    val secondaryStyle: LearningStyleType? = null,
    val confidenceScore: Double,
    val observationCount: Int,
    val lastUpdatedEpochMs: Long
) : ValueObject {

    init {
        require(confidenceScore in 0.0..1.0) {
            "Confidence score must be in range [0.0, 1.0] (got $confidenceScore)"
        }
        require(observationCount >= 0) {
            "Observation count cannot be negative (got $observationCount)"
        }
        require(lastUpdatedEpochMs > 0) {
            "Last updated timestamp must be a positive epoch value (got $lastUpdatedEpochMs)"
        }
        if (primaryStyle == LearningStyleType.UNDETERMINED) {
            require(confidenceScore == 0.0) {
                "An UNDETERMINED style must have a confidence score of 0.0 (got $confidenceScore)"
            }
        }
        if (secondaryStyle != null) {
            require(secondaryStyle != primaryStyle) {
                "Secondary style must differ from primary style (got $primaryStyle == $secondaryStyle)"
            }
            require(secondaryStyle != LearningStyleType.UNDETERMINED) {
                "Secondary style cannot be UNDETERMINED"
            }
        }
    }

    /** Returns true if the learning style has been inferred with sufficient confidence. */
    val isDetermined: Boolean get() = primaryStyle != LearningStyleType.UNDETERMINED

    /** Returns true if the style inference is considered reliable (confidence ≥ 0.7). */
    val isHighConfidence: Boolean get() = confidenceScore >= 0.7

    companion object {

        /** Factory: Creates a default undetermined preference for a brand-new learner. */
        fun undetermined(nowEpochMs: Long): LearningPreference = LearningPreference(
            primaryStyle = LearningStyleType.UNDETERMINED,
            secondaryStyle = null,
            confidenceScore = 0.0,
            observationCount = 0,
            lastUpdatedEpochMs = nowEpochMs
        )
    }
}
