package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.MemoryReview
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.WeaknessSeverity

// =====================================================================
// Learning Memory — LearningWeaknessAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Scans per-card memory records, groups them by topic,
// and identifies topics that qualify as learner Weaknesses based on:
//   • Average retention ≤ 0.60 (MAX_RETENTION_THRESHOLD)
//   • OR failure rate (FORGOTTEN cards / total cards) ≥ 0.30 (MIN_FAILURE_RATE_THRESHOLD)
//   • OR any card in topic has ≥ 3 consecutive review failures
//
// Severity classification:
//   MILD     → retention < 0.70 but above critical; failure rate < 0.30
//   MODERATE → retention ≤ 0.60 OR failure rate ≥ 0.30
//   SEVERE   → consecutive failure count ≥ 3 on any card in topic
//
// Determinism guarantee:
//   Same MemoryAnalysisInput → same List<LearningWeakness> output.
//   No randomness. No side effects. No repository access.
// =====================================================================

/**
 * Responsibility: Analyzes memory records to deterministically detect
 * topics where the learner has demonstrated consistent difficulty.
 *
 * Returns a complete list of all topics currently qualifying as weaknesses.
 * Topics above the threshold are omitted from the result.
 *
 * Fully stateless — no mutable instance state.
 */
object LearningWeaknessAnalyzer {

    /**
     * Minimum number of cards that must be tracked for a topic before
     * it can qualify as a Weakness. Prevents false positives from single-card topics.
     */
    const val MIN_CARDS_PER_TOPIC: Int = 1

    /**
     * Retention level below which a topic starts being monitored (pre-weakness zone).
     * Topics in [MILD_RETENTION_THRESHOLD, MAX_RETENTION_THRESHOLD) are MILD weaknesses.
     */
    const val MILD_RETENTION_THRESHOLD: Double = 0.70

    /**
     * Analyzes [input] memory records to find all topics that qualify as Weaknesses.
     *
     * @param input              The shared memory analysis input.
     * @param existingWeaknesses The current list of weaknesses, used to preserve
     *                           [LearningWeakness.detectedEpochMs] and
     *                           [LearningWeakness.lastReinforcedEpochMs] for known topics.
     * @return A list of [LearningWeakness] for all topics that currently meet
     *         the weakness criteria. Topics that have recovered are excluded.
     */
    fun analyze(
        input: MemoryAnalysisInput,
        existingWeaknesses: List<LearningWeakness>
    ): List<LearningWeakness> {
        if (input.memoryRecords.isEmpty()) return emptyList()

        val byTopic = input.recordsByTopic()
        val existingByCode = existingWeaknesses.associateBy { it.topicCode }

        val result = mutableListOf<LearningWeakness>()

        for ((topicCode, records) in byTopic) {
            if (!topicCode.startsWith("tp_")) continue
            if (records.size < MIN_CARDS_PER_TOPIC) continue

            val totalCards = records.size
            val forgottenCards = records.count {
                it.state == MemoryState.FORGOTTEN || it.state == MemoryState.RECOVERED
            }
            val failureRate = forgottenCards.toDouble() / totalCards
            val averageRetention = records.map { it.retention }.average()

            // Compute max consecutive failures across all cards in this topic
            val maxConsecutiveFailures = records.maxOfOrNull { record ->
                computeConsecutiveFailures(record.reviews)
            } ?: 0

            // Determine if this topic qualifies as a weakness
            val qualifiesAsWeak =
                averageRetention <= LearningWeakness.MAX_RETENTION_THRESHOLD ||
                        failureRate >= LearningWeakness.MIN_FAILURE_RATE_THRESHOLD ||
                        maxConsecutiveFailures >= LearningWeakness.SEVERE_CONSECUTIVE_THRESHOLD

            // Also flag topics that are entering the pre-weakness zone (MILD)
            val isInMildZone = averageRetention < MILD_RETENTION_THRESHOLD &&
                    averageRetention > LearningWeakness.MAX_RETENTION_THRESHOLD

            if (!qualifiesAsWeak && !isInMildZone) continue

            val topicName = input.cardToTopicNameMapper(records.first().cardId)
            val existing = existingByCode[topicCode]
            val detectedEpochMs = existing?.detectedEpochMs ?: input.nowEpochMs
            val lastReinforcedEpochMs = existing?.lastReinforcedEpochMs

            val severity = classifySeverity(
                averageRetention = averageRetention,
                failureRate = failureRate,
                maxConsecutiveFailures = maxConsecutiveFailures
            )

            result.add(
                LearningWeakness(
                    topicCode = topicCode,
                    topicName = topicName,
                    averageRetention = averageRetention,
                    failureRate = failureRate,
                    forgottenCardCount = forgottenCards,
                    totalCardCount = totalCards,
                    consecutiveFailureCount = maxConsecutiveFailures,
                    severity = severity,
                    detectedEpochMs = detectedEpochMs,
                    lastReinforcedEpochMs = lastReinforcedEpochMs
                )
            )
        }

        // Sort: SEVERE first, then MODERATE, then MILD; within tier by lowest retention
        return result.sortedWith(
            compareByDescending<LearningWeakness> { it.severity.ordinal }
                .thenBy { it.averageRetention }
                .thenBy { it.topicCode }
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Classifies the severity of a detected weakness based on retention,
     * failure rate, and consecutive failure count.
     */
    internal fun classifySeverity(
        averageRetention: Double,
        failureRate: Double,
        maxConsecutiveFailures: Int
    ): WeaknessSeverity {
        return when {
            maxConsecutiveFailures >= LearningWeakness.SEVERE_CONSECUTIVE_THRESHOLD ->
                WeaknessSeverity.SEVERE
            failureRate >= LearningWeakness.MIN_FAILURE_RATE_THRESHOLD ||
                    averageRetention <= LearningWeakness.MAX_RETENTION_THRESHOLD ->
                WeaknessSeverity.MODERATE
            else ->
                WeaknessSeverity.MILD
        }
    }

    /**
     * Computes the maximum streak of consecutive failed reviews (accuracy < 0.6)
     * in a review history, scanning the list from oldest to newest.
     */
    internal fun computeConsecutiveFailures(reviews: List<MemoryReview>): Int {
        if (reviews.isEmpty()) return 0
        var maxStreak = 0
        var currentStreak = 0
        for (review in reviews.sortedBy { it.timestamp }) {
            if (review.accuracy < 0.6) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }
        return maxStreak
    }
}
