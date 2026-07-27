package com.learnwitme.core.domain.model.memory

import com.learnwitme.core.domain.model.ValueObject
import kotlinx.serialization.Serializable

// =====================================================================
// Learning Memory — LearningPattern
// Sprint 9 · Stage 1 · Learning Memory Foundation
//
// Responsibility: Stores deterministic, observable learning habits
// for a specific learner. Updated by the Memory Engine after analyzing
// session data from the Progress Engine.
//
// Patterns are statistical accumulations — not configuration.
// =====================================================================

/**
 * Enumerates the broad time-of-day learning window.
 */
enum class StudyTimeWindow {
    EARLY_MORNING,   // 04:00–07:59
    MORNING,         // 08:00–11:59
    AFTERNOON,       // 12:00–15:59
    LATE_AFTERNOON,  // 16:00–18:59
    EVENING,         // 19:00–21:59
    NIGHT,           // 22:00–23:59
    UNKNOWN
}

/**
 * Responsibility: Stores deterministic learning habits derived from
 * observed session data over time.
 *
 * All fields are computed and updated by the Memory Engine.
 * No field is manually configured by the user.
 *
 * Invariants:
 * - [averageSessionDurationMs] must be non-negative.
 * - [completionRate] must be in range [0.0, 1.0].
 * - [preferredDifficulty] and [bestStudyWindow] are enums and always valid.
 * - [sessionCount] must be non-negative.
 * - [lastComputedEpochMs] must be positive.
 *
 * @param bestStudyWindow       Observed time-of-day window when the learner is most consistent.
 * @param averageSessionDurationMs Average duration of completed study sessions in milliseconds.
 * @param completionRate        Ratio of completed sessions to started sessions (0.0–1.0).
 * @param preferredDifficulty   Most frequently selected or best-performing difficulty level.
 * @param sessionCount          Total number of sessions analyzed to produce this pattern.
 * @param lastComputedEpochMs   Epoch timestamp (ms) when this pattern was last recomputed.
 */
@Serializable
data class LearningPattern(
    val bestStudyWindow: StudyTimeWindow,
    val averageSessionDurationMs: Long,
    val completionRate: Double,
    val preferredDifficulty: PatternDifficultyLevel,
    val sessionCount: Int,
    val lastComputedEpochMs: Long
) : ValueObject {

    init {
        require(averageSessionDurationMs >= 0) {
            "Average session duration cannot be negative (got $averageSessionDurationMs)"
        }
        require(completionRate in 0.0..1.0) {
            "Completion rate must be in range [0.0, 1.0] (got $completionRate)"
        }
        require(sessionCount >= 0) {
            "Session count cannot be negative (got $sessionCount)"
        }
        require(lastComputedEpochMs > 0) {
            "Last computed timestamp must be a positive epoch value (got $lastComputedEpochMs)"
        }
    }

    /** Returns true if enough sessions have been accumulated for a reliable pattern (≥ 5 sessions). */
    val isReliable: Boolean get() = sessionCount >= 5

    /** Returns average session duration as a human-readable minute count. */
    val averageSessionDurationMinutes: Long get() = averageSessionDurationMs / 60_000L

    companion object {
        /** Factory: Creates a default empty pattern for a brand-new learner. */
        fun empty(nowEpochMs: Long): LearningPattern = LearningPattern(
            bestStudyWindow = StudyTimeWindow.UNKNOWN,
            averageSessionDurationMs = 0L,
            completionRate = 0.0,
            preferredDifficulty = PatternDifficultyLevel.UNKNOWN,
            sessionCount = 0,
            lastComputedEpochMs = nowEpochMs
        )
    }
}

/**
 * Difficulty level representation within the pattern context.
 * Isolated from the global DifficultyLevel to avoid cross-context coupling.
 */
enum class PatternDifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    MIXED,
    UNKNOWN
}
