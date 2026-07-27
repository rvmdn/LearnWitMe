package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningBehavior
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Tracks observable, recurring study behaviors
// inferred from patterns in session interaction data.
//
// Behaviors are descriptive signals — not judgments.
// They feed the Recommendation Engine and AI Enhancement Layer
// to personalize content delivery without hardcoding assumptions.
// =====================================================================

/**
 * Classifies a specific observable behavior type.
 *
 * Each variant maps directly to a behavioral signal observable in
 * session interaction data from the Progress Engine.
 */
enum class BehaviorType {

    /** Learner repeatedly skips quiz items without answering. */
    FREQUENTLY_SKIPS_QUIZZES,

    /** Learner reads explanation cards more than once per session. */
    READS_EXPLANATIONS_MULTIPLE_TIMES,

    /** Learner consistently revisits a specific topic even without scheduled reviews. */
    OFTEN_REVISITS_TOPIC,

    /** Learner consistently completes sessions at or above the planned duration. */
    COMPLETES_SESSIONS_CONSISTENTLY,

    /** Learner frequently abandons sessions before completion. */
    FREQUENTLY_ABANDONS_SESSIONS,

    /** Learner uses hints on a majority of quiz attempts. */
    HIGH_HINT_USAGE,

    /** Learner answers quiz items very quickly — may indicate guessing. */
    RAPID_QUIZ_RESPONSES,

    /** Learner takes considerably longer than average on quiz items. */
    SLOW_QUIZ_RESPONSES,

    /** Learner consistently reviews material well ahead of scheduled dates. */
    PROACTIVE_REVIEWER,

    /** Learner consistently reviews material significantly past scheduled dates. */
    OVERDUE_REVIEWER
}

/**
 * Responsibility: Represents a single observable learning behavior
 * that the Memory Engine has detected and recorded for a learner.
 *
 * Behaviors are updated with each new session analysis.
 * A behavior with insufficient evidence is removed from the record.
 *
 * Invariants:
 * - [occurrenceCount] must be positive.
 * - [occurrenceRate] must be in range [0.0, 1.0].
 * - [firstObservedEpochMs] must be positive.
 * - [lastObservedEpochMs] must be ≥ [firstObservedEpochMs].
 * - If [relatedTopicCode] is provided, it must start with "tp_".
 *
 * @param type                Observable behavior classification.
 * @param occurrenceCount     Total number of times this behavior was observed.
 * @param occurrenceRate      Rate of occurrence relative to total observed sessions (0.0–1.0).
 * @param firstObservedEpochMs Epoch timestamp (ms) of the first occurrence.
 * @param lastObservedEpochMs  Epoch timestamp (ms) of the most recent occurrence.
 * @param relatedTopicCode    Optional topic code this behavior is specifically associated with.
 */
@Serializable
data class LearningBehavior(
    val type: BehaviorType,
    val occurrenceCount: Int,
    val occurrenceRate: Double,
    val firstObservedEpochMs: Long,
    val lastObservedEpochMs: Long,
    val relatedTopicCode: String? = null
) : ValueObject {

    init {
        require(occurrenceCount > 0) {
            "Occurrence count must be positive — zero occurrences should not be stored (got $occurrenceCount)"
        }
        require(occurrenceRate in 0.0..1.0) {
            "Occurrence rate must be in range [0.0, 1.0] (got $occurrenceRate)"
        }
        require(firstObservedEpochMs > 0) {
            "First observed timestamp must be a positive epoch value (got $firstObservedEpochMs)"
        }
        require(lastObservedEpochMs > 0) {
            "Last observed timestamp must be a positive epoch value (got $lastObservedEpochMs)"
        }
        require(lastObservedEpochMs >= firstObservedEpochMs) {
            "Last observed timestamp ($lastObservedEpochMs) cannot be before first observed timestamp ($firstObservedEpochMs)"
        }
        if (relatedTopicCode != null) {
            require(relatedTopicCode.isNotBlank()) { "Related topic code cannot be blank if provided" }
            require(relatedTopicCode.startsWith("tp_")) {
                "Related topic code must start with 'tp_' (got '$relatedTopicCode')"
            }
        }
    }

    /** Returns true if this behavior is topic-specific (vs. general study habit). */
    val isTopicSpecific: Boolean get() = relatedTopicCode != null

    /** Returns true if this behavior is considered frequent (occurrence rate ≥ 0.5). */
    val isFrequent: Boolean get() = occurrenceRate >= 0.5
}
