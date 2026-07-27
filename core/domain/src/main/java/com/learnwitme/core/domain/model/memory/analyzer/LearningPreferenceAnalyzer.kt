package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.LearningPreference

// =====================================================================
// Learning Memory — LearningPreferenceAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Detects the learner's dominant cognitive learning
// style by analyzing interaction signals from session observations.
//
// Detection logic:
//   VISUAL       → high read counts relative to session duration;
//                  low quiz skip rate; average accuracy stays high.
//   PRACTICAL    → interactions start with quizzes; low theory-read
//                  count per session; high quiz engagement rate.
//   THEORY_FIRST → reads cards multiple times before any quiz;
//                  high read count precedes quiz interactions.
//   EXAMPLE_FIRST→ revisits cards repeatedly before attempting quiz;
//                  reads 'realWorldExample' content type more often.
//
// Determinism guarantee:
//   Same MemoryAnalysisInput → same LearningPreference output.
//   No randomness. No side effects. No repository access.
// =====================================================================

/**
 * Responsibility: Analyzes session interaction patterns to deterministically
 * infer the learner's dominant cognitive learning style.
 *
 * Algorithm:
 * For each session, score each style dimension from observable signals.
 * Accumulate scores across sessions. Normalize. The highest-scoring
 * style above the minimum confidence threshold becomes the primary.
 * The second-highest (if above a secondary threshold) becomes optional secondary.
 *
 * Fully stateless — no mutable state, no field state.
 */
object LearningPreferenceAnalyzer {

    /**
     * Minimum number of sessions required before a style can be inferred.
     * Below this threshold, UNDETERMINED is always returned.
     */
    const val MIN_SESSIONS_FOR_INFERENCE: Int = 3

    /**
     * Minimum confidence score (0.0–1.0) required for a style to be considered primary.
     */
    const val MIN_PRIMARY_CONFIDENCE: Double = 0.30

    /**
     * Minimum confidence score for a style to be recorded as secondary.
     */
    const val MIN_SECONDARY_CONFIDENCE: Double = 0.20

    /**
     * Analyzes the [input] to infer the learner's dominant learning style.
     *
     * @param input   The shared memory analysis input.
     * @param existingPreference The current preference record, used to preserve
     *                           the [LearningPreference.observationCount] baseline.
     * @return A new [LearningPreference] reflecting the updated inference.
     *         Returns UNDETERMINED if insufficient data is available.
     */
    fun analyze(
        input: MemoryAnalysisInput,
        existingPreference: LearningPreference
    ): LearningPreference {

        val sessions = input.recentSessions
        val totalSessions = sessions.size

        // Guard: insufficient data
        if (totalSessions < MIN_SESSIONS_FOR_INFERENCE) {
            return LearningPreference.undetermined(nowEpochMs = input.nowEpochMs)
        }

        // Accumulate weighted style scores across sessions
        val scores = mutableMapOf(
            LearningStyleType.VISUAL to 0.0,
            LearningStyleType.PRACTICAL to 0.0,
            LearningStyleType.THEORY_FIRST to 0.0,
            LearningStyleType.EXAMPLE_FIRST to 0.0
        )

        for (session in sessions) {
            val interactions = session.cardInteractions
            if (interactions.isEmpty()) continue

            val quizInteractions = interactions.filter { it.isQuiz }
            val readInteractions = interactions.filter { !it.isQuiz }

            val totalInteractions = interactions.size.toDouble()
            val quizCount = quizInteractions.size.toDouble()
            val readCount = readInteractions.size.toDouble()

            // ── VISUAL signal: high read engagement + low skip rate + good accuracy
            val avgReadCount = if (readInteractions.isNotEmpty())
                readInteractions.map { it.readCount }.average() else 0.0
            val skipRate = interactions.count { it.wasSkipped } / totalInteractions
            val avgAccuracy = if (interactions.isNotEmpty())
                interactions.map { it.accuracy }.average() else 0.0

            val visualScore = (avgReadCount.coerceAtMost(3.0) / 3.0 * 0.4) +
                    ((1.0 - skipRate) * 0.3) +
                    (avgAccuracy * 0.3)
            scores[LearningStyleType.VISUAL] = scores[LearningStyleType.VISUAL]!! + visualScore

            // ── PRACTICAL signal: starts with or prefers quizzes; high quiz-to-read ratio
            val quizRatio = if (totalInteractions > 0) quizCount / totalInteractions else 0.0
            val firstInteractionIsQuiz = interactions.firstOrNull()?.isQuiz ?: false
            val practicalScore = (quizRatio * 0.5) +
                    (if (firstInteractionIsQuiz) 0.3 else 0.0) +
                    ((1.0 - skipRate) * 0.2)
            scores[LearningStyleType.PRACTICAL] = scores[LearningStyleType.PRACTICAL]!! + practicalScore

            // ── THEORY_FIRST signal: multiple reads before quiz; reads precede quizzes chronologically
            val firstQuizIndex = interactions.indexOfFirst { it.isQuiz }
            val readsBeforeFirstQuiz = if (firstQuizIndex > 0) {
                interactions.take(firstQuizIndex).count { !it.isQuiz }
            } else if (firstQuizIndex == -1) readInteractions.size else 0
            val theoryReadRatio = if (readCount > 0)
                (readsBeforeFirstQuiz.toDouble() / readCount).coerceAtMost(1.0) else 0.0
            val avgReadCountForTheory = if (readInteractions.isNotEmpty())
                readInteractions.map { it.readCount }.average() else 0.0
            val theoryScore = (theoryReadRatio * 0.5) +
                    ((avgReadCountForTheory.coerceAtMost(2.0) / 2.0) * 0.3) +
                    (readCount / totalInteractions * 0.2)
            scores[LearningStyleType.THEORY_FIRST] = scores[LearningStyleType.THEORY_FIRST]!! + theoryScore

            // ── EXAMPLE_FIRST signal: high re-read count on individual cards before quiz
            val avgReadsPerCard = if (readInteractions.isNotEmpty())
                readInteractions.map { it.readCount }.average() else 0.0
            val exampleScore = ((avgReadsPerCard.coerceAtMost(4.0) / 4.0) * 0.6) +
                    (readCount / totalInteractions.coerceAtLeast(1.0) * 0.4)
            scores[LearningStyleType.EXAMPLE_FIRST] = scores[LearningStyleType.EXAMPLE_FIRST]!! + exampleScore
        }

        // Normalize scores to [0.0, 1.0] per style across all sessions
        val normalizedScores = scores.mapValues { (_, total) ->
            (total / totalSessions).coerceIn(0.0, 1.0)
        }

        // Sort styles by score descending
        val ranked = normalizedScores.entries.sortedByDescending { it.value }
        val topStyle = ranked[0]
        val secondStyle = ranked.getOrNull(1)

        // Apply confidence thresholds
        if (topStyle.value < MIN_PRIMARY_CONFIDENCE) {
            return LearningPreference.undetermined(nowEpochMs = input.nowEpochMs)
        }

        val primary = topStyle.key
        val secondary = if (secondStyle != null &&
            secondStyle.value >= MIN_SECONDARY_CONFIDENCE &&
            secondStyle.key != LearningStyleType.UNDETERMINED
        ) secondStyle.key else null

        val totalObservations = existingPreference.observationCount + totalSessions

        return LearningPreference(
            primaryStyle = primary,
            secondaryStyle = secondary,
            confidenceScore = topStyle.value.coerceIn(0.0, 1.0),
            observationCount = totalObservations,
            lastUpdatedEpochMs = input.nowEpochMs
        )
    }
}
