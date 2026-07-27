package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.memory.BehaviorType
import com.learnwitme.core.domain.model.memory.LearningBehavior

// =====================================================================
// Learning Memory — LearningBehaviorAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Detects observable, recurring study behaviors from
// session observation data. Each BehaviorType is evaluated independently
// against its own detection rule and occurrence threshold.
//
// Detection rules (per BehaviorType):
//   FREQUENTLY_SKIPS_QUIZZES        → skip rate on quizzes ≥ 0.40 across sessions
//   READS_EXPLANATIONS_MULTIPLE_TIMES → avg read count per card > 1.5
//   OFTEN_REVISITS_TOPIC            → same topic appears in > 50% of sessions (per topic)
//   COMPLETES_SESSIONS_CONSISTENTLY → session completion rate ≥ 0.80
//   FREQUENTLY_ABANDONS_SESSIONS    → session completion rate ≤ 0.30
//   HIGH_HINT_USAGE                 → hint usage rate on quiz interactions ≥ 0.50
//   RAPID_QUIZ_RESPONSES            → avg quiz response time < 5 seconds
//   SLOW_QUIZ_RESPONSES             → avg quiz response time > 60 seconds
//   PROACTIVE_REVIEWER              → sessions start well before scheduled (not yet detectable
//                                     from current input; reserved for future extension)
//   OVERDUE_REVIEWER                → sessions are consistently late (reserved)
//
// Note: PROACTIVE_REVIEWER and OVERDUE_REVIEWER require review schedule data
// which is outside the current input scope. They are detected as absent.
//
// Determinism guarantee:
//   Same MemoryAnalysisInput → same List<LearningBehavior> output.
//   No randomness. No side effects. No repository access.
// =====================================================================

/**
 * Responsibility: Analyzes session interaction data to deterministically
 * detect recurring observable learning behaviors.
 *
 * Each BehaviorType is evaluated independently.
 * Behaviors that don't meet their detection threshold are excluded from the result.
 * A behavior's [LearningBehavior.occurrenceRate] reflects how consistently it appeared.
 *
 * Fully stateless — no mutable instance state.
 */
object LearningBehaviorAnalyzer {

    /**
     * Minimum sessions required before behavior analysis is meaningful.
     */
    const val MIN_SESSIONS_FOR_ANALYSIS: Int = 2

    /**
     * Analyzes [input] session observations to detect recurring behaviors.
     *
     * @param input              The shared memory analysis input.
     * @param existingBehaviors  The current list of detected behaviors, used to
     *                           preserve [LearningBehavior.firstObservedEpochMs].
     * @return A list of [LearningBehavior] for all behaviors currently detected.
     *         Returns an empty list if insufficient data is available.
     */
    fun analyze(
        input: MemoryAnalysisInput,
        existingBehaviors: List<LearningBehavior>
    ): List<LearningBehavior> {
        val sessions = input.recentSessions
        if (sessions.size < MIN_SESSIONS_FOR_ANALYSIS) return emptyList()

        val existingByType = existingBehaviors.associateBy { it.type }
        val result = mutableListOf<LearningBehavior>()
        val nowEpochMs = input.nowEpochMs

        // Helper: builds a LearningBehavior if occurrenceRate meets the threshold
        fun emitBehavior(
            type: BehaviorType,
            occurrenceCount: Int,
            occurrenceRate: Double,
            threshold: Double,
            relatedTopicCode: String? = null
        ) {
            if (occurrenceRate < threshold) return
            val existing = existingByType[type]
            result.add(
                LearningBehavior(
                    type = type,
                    occurrenceCount = occurrenceCount,
                    occurrenceRate = occurrenceRate.coerceIn(0.0, 1.0),
                    firstObservedEpochMs = existing?.firstObservedEpochMs ?: nowEpochMs,
                    lastObservedEpochMs = nowEpochMs,
                    relatedTopicCode = relatedTopicCode
                )
            )
        }

        val totalSessions = sessions.size.toDouble()
        val allInteractions = input.allInteractions
        val allQuizInteractions = input.allQuizInteractions

        // ── FREQUENTLY_SKIPS_QUIZZES ──────────────────────────────────
        val totalQuizInteractions = allQuizInteractions.size
        val skippedQuizCount = allQuizInteractions.count { it.wasSkipped }
        if (totalQuizInteractions > 0) {
            val skipRate = skippedQuizCount.toDouble() / totalQuizInteractions
            emitBehavior(
                type = BehaviorType.FREQUENTLY_SKIPS_QUIZZES,
                occurrenceCount = skippedQuizCount,
                occurrenceRate = skipRate,
                threshold = 0.40
            )
        }

        // ── READS_EXPLANATIONS_MULTIPLE_TIMES ─────────────────────────
        val readInteractions = allInteractions.filter { !it.isQuiz }
        if (readInteractions.isNotEmpty()) {
            val avgReadCount = readInteractions.map { it.readCount }.average()
            val multiReadSessions = sessions.count { session ->
                session.cardInteractions.filter { !it.isQuiz }.any { it.readCount > 1 }
            }
            val multiReadRate = multiReadSessions / totalSessions
            if (avgReadCount > 1.5) {
                emitBehavior(
                    type = BehaviorType.READS_EXPLANATIONS_MULTIPLE_TIMES,
                    occurrenceCount = readInteractions.count { it.readCount > 1 },
                    occurrenceRate = multiReadRate,
                    threshold = 0.30
                )
            }
        }

        // ── OFTEN_REVISITS_TOPIC ──────────────────────────────────────
        // Find any topic that appears in > 50% of sessions as non-scheduled revisit
        val topicSessionFrequency = mutableMapOf<String, Int>()
        for (session in sessions) {
            val topicsInSession = session.cardInteractions
                .filter { !it.wasSkipped }
                .map { it.topicCode }
                .toSet()
            for (topic in topicsInSession) {
                topicSessionFrequency[topic] = (topicSessionFrequency[topic] ?: 0) + 1
            }
        }
        val mostRevisitedEntry = topicSessionFrequency.maxByOrNull { it.value }
        if (mostRevisitedEntry != null) {
            val revisitRate = mostRevisitedEntry.value / totalSessions
            val revisitCount = topicSessionFrequency.values.sum()
            emitBehavior(
                type = BehaviorType.OFTEN_REVISITS_TOPIC,
                occurrenceCount = revisitCount,
                occurrenceRate = revisitRate,
                threshold = 0.50,
                relatedTopicCode = mostRevisitedEntry.key.takeIf { it.startsWith("tp_") }
            )
        }

        // ── COMPLETES_SESSIONS_CONSISTENTLY ──────────────────────────
        val completedCount = sessions.count { it.completed }
        val completionRate = completedCount / totalSessions
        emitBehavior(
            type = BehaviorType.COMPLETES_SESSIONS_CONSISTENTLY,
            occurrenceCount = completedCount,
            occurrenceRate = completionRate,
            threshold = 0.80
        )

        // ── FREQUENTLY_ABANDONS_SESSIONS ──────────────────────────────
        val abandonedCount = sessions.count { !it.completed }
        val abandonRate = abandonedCount / totalSessions
        emitBehavior(
            type = BehaviorType.FREQUENTLY_ABANDONS_SESSIONS,
            occurrenceCount = abandonedCount,
            occurrenceRate = abandonRate,
            threshold = 0.30
        )

        // ── HIGH_HINT_USAGE ───────────────────────────────────────────
        if (allQuizInteractions.isNotEmpty()) {
            val hintCount = allQuizInteractions.count { it.hintUsed }
            val hintRate = hintCount / allQuizInteractions.size.toDouble()
            emitBehavior(
                type = BehaviorType.HIGH_HINT_USAGE,
                occurrenceCount = hintCount,
                occurrenceRate = hintRate,
                threshold = 0.50
            )
        }

        // ── RAPID_QUIZ_RESPONSES ──────────────────────────────────────
        val answeredQuizzes = allQuizInteractions.filter { !it.wasSkipped }
        if (answeredQuizzes.isNotEmpty()) {
            val avgResponseMs = answeredQuizzes.map { it.responseTimeMs }.average()
            if (avgResponseMs < 5_000L) { // < 5 seconds
                val rapidCount = answeredQuizzes.count { it.responseTimeMs < 5_000L }
                val rapidRate = rapidCount / answeredQuizzes.size.toDouble()
                emitBehavior(
                    type = BehaviorType.RAPID_QUIZ_RESPONSES,
                    occurrenceCount = rapidCount,
                    occurrenceRate = rapidRate,
                    threshold = 0.50
                )
            }

            // ── SLOW_QUIZ_RESPONSES ───────────────────────────────────
            if (avgResponseMs > 60_000L) { // > 60 seconds
                val slowCount = answeredQuizzes.count { it.responseTimeMs > 60_000L }
                val slowRate = slowCount / answeredQuizzes.size.toDouble()
                emitBehavior(
                    type = BehaviorType.SLOW_QUIZ_RESPONSES,
                    occurrenceCount = slowCount,
                    occurrenceRate = slowRate,
                    threshold = 0.40
                )
            }
        }

        // ── PROACTIVE_REVIEWER / OVERDUE_REVIEWER ─────────────────────
        // These require review schedule timestamps not available in current input.
        // Intentionally not detected — no data, no inference. Deterministic silence.

        // Sort by BehaviorType ordinal for stable ordering
        return result.sortedBy { it.type.ordinal }
    }
}
