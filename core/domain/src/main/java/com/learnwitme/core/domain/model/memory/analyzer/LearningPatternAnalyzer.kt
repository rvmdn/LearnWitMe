package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.PatternDifficultyLevel
import com.learnwitme.core.domain.model.memory.StudyTimeWindow
import com.learnwitme.core.domain.model.DifficultyLevel

// =====================================================================
// Learning Memory — LearningPatternAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Detects deterministic study habits from observable
// session data:
//   • Best study time window (time-of-day when sessions are most consistent)
//   • Average session duration
//   • Session completion rate
//   • Preferred difficulty level
//
// Determinism guarantee:
//   Same MemoryAnalysisInput → same LearningPattern output.
//   No randomness. No side effects. No repository access.
// =====================================================================

/**
 * Responsibility: Analyzes session history to deterministically compute
 * the learner's behavioral study pattern.
 *
 * All computations are statistical aggregations over the provided sessions.
 * Fully stateless — no mutable instance state.
 */
object LearningPatternAnalyzer {

    /**
     * Minimum sessions required before the pattern is considered reliable.
     * Below this count, the pattern is computed but flagged as unreliable.
     */
    const val MIN_SESSIONS_FOR_RELIABILITY: Int = 5

    /**
     * Analyzes [input] session observations to compute the learner's study pattern.
     *
     * @param input The shared memory analysis input.
     * @return A [LearningPattern] reflecting the deterministic behavioral pattern.
     *         Returns an empty pattern if no sessions are available.
     */
    fun analyze(input: MemoryAnalysisInput): LearningPattern {
        val sessions = input.recentSessions

        if (sessions.isEmpty()) {
            return LearningPattern.empty(nowEpochMs = input.nowEpochMs)
        }

        val sessionCount = sessions.size

        // ── Average session duration ───────────────────────────────────
        val avgDurationMs = sessions.map { it.durationMs }.average().toLong()

        // ── Completion rate ────────────────────────────────────────────
        val completedSessions = sessions.count { it.completed }
        val completionRate = completedSessions.toDouble() / sessionCount

        // ── Best study time window ─────────────────────────────────────
        // Frequency-based: the time window with the most sessions started is the "best".
        val windowFrequency = mutableMapOf<StudyTimeWindow, Int>()
        for (session in sessions) {
            val window = epochMsToStudyWindow(session.startEpochMs)
            windowFrequency[window] = (windowFrequency[window] ?: 0) + 1
        }
        val bestStudyWindow = windowFrequency
            .filter { (window, _) -> window != StudyTimeWindow.UNKNOWN }
            .maxByOrNull { (_, count) -> count }?.key ?: StudyTimeWindow.UNKNOWN

        // ── Preferred difficulty ───────────────────────────────────────
        val preferredDifficulty = computePreferredDifficulty(sessions)

        return LearningPattern(
            bestStudyWindow = bestStudyWindow,
            averageSessionDurationMs = avgDurationMs,
            completionRate = completionRate,
            preferredDifficulty = preferredDifficulty,
            sessionCount = sessionCount,
            lastComputedEpochMs = input.nowEpochMs
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Maps an epoch ms timestamp to the local hour-of-day study window.
     * Uses UTC offset-agnostic hour extraction (based on modulo arithmetic on epoch).
     *
     * This is intentionally simple and deterministic — no timezone resolution.
     * A timezone-aware version would require an infrastructure-level clock,
     * which would violate the pure-domain constraint.
     */
    internal fun epochMsToStudyWindow(epochMs: Long): StudyTimeWindow {
        val totalSeconds = epochMs / 1000L
        val secondsInDay = totalSeconds % (24 * 3600L)
        val hourOfDay = (secondsInDay / 3600L).toInt()

        return when (hourOfDay) {
            in 4..7 -> StudyTimeWindow.EARLY_MORNING
            in 8..11 -> StudyTimeWindow.MORNING
            in 12..15 -> StudyTimeWindow.AFTERNOON
            in 16..18 -> StudyTimeWindow.LATE_AFTERNOON
            in 19..21 -> StudyTimeWindow.EVENING
            in 22..23, in 0..3 -> StudyTimeWindow.NIGHT
            else -> StudyTimeWindow.UNKNOWN
        }
    }

    /**
     * Determines the preferred difficulty by aggregating which difficulty level
     * corresponds to the sessions with the highest accuracy interactions.
     *
     * Difficulty is inferred from the proportion of interactions per accuracy tier:
     * - High accuracy (≥ 0.85) → learner performs well → leaning ADVANCED
     * - Mid accuracy (0.60–0.84) → comfortable → INTERMEDIATE
     * - Low accuracy (< 0.60) → struggling → BEGINNER
     *
     * If sessions are mixed across tiers, returns MIXED.
     */
    internal fun computePreferredDifficulty(
        sessions: List<SessionObservation>
    ): PatternDifficultyLevel {
        val allInteractions = sessions.flatMap { it.cardInteractions }.filter { !it.wasSkipped }

        if (allInteractions.isEmpty()) return PatternDifficultyLevel.UNKNOWN

        val avgAccuracy = allInteractions.map { it.accuracy }.average()
        val highAccuracyRate = allInteractions.count { it.accuracy >= 0.85 }.toDouble() / allInteractions.size
        val lowAccuracyRate = allInteractions.count { it.accuracy < 0.60 }.toDouble() / allInteractions.size

        return when {
            highAccuracyRate >= 0.6 -> PatternDifficultyLevel.ADVANCED
            lowAccuracyRate >= 0.4 -> PatternDifficultyLevel.BEGINNER
            avgAccuracy in 0.60..0.84 && highAccuracyRate < 0.6 && lowAccuracyRate < 0.4 -> PatternDifficultyLevel.INTERMEDIATE
            else -> PatternDifficultyLevel.MIXED
        }
    }
}
