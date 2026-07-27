package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.memory.LearningStrength

// =====================================================================
// Learning Memory — LearningStrengthAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Scans per-card memory records, groups them by topic,
// and identifies topics that qualify as learner Strengths based on:
//   • Average retention across all cards in the topic ≥ 0.85
//   • Mastery rate (cards in MASTERED state / total cards) ≥ 0.80
//   • Minimum card count per topic ≥ MIN_CARDS_PER_TOPIC
//
// A topic that previously appeared as a weakness CANNOT simultaneously
// be promoted to strength — that business rule is enforced in the
// LearningMemory aggregate, not in this analyzer.
//
// Determinism guarantee:
//   Same MemoryAnalysisInput → same List<LearningStrength> output.
//   No randomness. No side effects. No repository access.
// =====================================================================

/**
 * Responsibility: Analyzes memory records to deterministically detect
 * topics where the learner has demonstrated consistent mastery.
 *
 * Returns a complete list of all topics currently qualifying as strengths.
 * Topics below the threshold are omitted from the result.
 *
 * Fully stateless — no mutable instance state.
 */
object LearningStrengthAnalyzer {

    /**
     * Minimum number of cards that must be tracked for a topic before
     * it can qualify as a Strength. Prevents false positives from single-card topics.
     */
    const val MIN_CARDS_PER_TOPIC: Int = 2

    /**
     * Analyzes [input] memory records to find all topics that qualify as Strengths.
     *
     * @param input             The shared memory analysis input.
     * @param existingStrengths The current list of strengths, used to preserve
     *                          [LearningStrength.detectedEpochMs] and
     *                          [LearningStrength.confirmedEpochMs] for known topics.
     * @return A list of [LearningStrength] for all topics that currently meet
     *         the strength threshold. Topics that no longer qualify are excluded.
     */
    fun analyze(
        input: MemoryAnalysisInput,
        existingStrengths: List<LearningStrength>
    ): List<LearningStrength> {
        if (input.memoryRecords.isEmpty()) return emptyList()

        // Group records by topic code
        val byTopic = input.recordsByTopic()
        val existingByCode = existingStrengths.associateBy { it.topicCode }

        val result = mutableListOf<LearningStrength>()

        for ((topicCode, records) in byTopic) {
            // Guard: topic code must follow ubiquitous language convention
            if (!topicCode.startsWith("tp_")) continue

            // Guard: minimum card count required for meaningful analysis
            if (records.size < MIN_CARDS_PER_TOPIC) continue

            val totalCards = records.size
            val masteredCards = records.count { it.state == MemoryState.MASTERED }
            val masteryRate = masteredCards.toDouble() / totalCards
            val averageRetention = records.map { it.retention }.average()

            // Apply strength thresholds from the domain model constants
            val qualifiesAsStrength =
                averageRetention >= LearningStrength.MIN_RETENTION_THRESHOLD &&
                        masteryRate >= LearningStrength.MIN_MASTERY_RATE

            if (!qualifiesAsStrength) continue

            // Retrieve topic name from the first record's card (via mapper)
            val topicName = if (records.isNotEmpty()) {
                input.cardToTopicNameMapper(records.first().cardId)
            } else topicCode

            // Preserve original detection timestamp if already known; otherwise set to now
            val existing = existingByCode[topicCode]
            val detectedEpochMs = existing?.detectedEpochMs ?: input.nowEpochMs

            // If already detected, this analysis re-confirms — update confirmedEpochMs
            val confirmedEpochMs = if (existing != null) input.nowEpochMs else null

            result.add(
                LearningStrength(
                    topicCode = topicCode,
                    topicName = topicName,
                    averageRetention = averageRetention,
                    masteryRate = masteryRate,
                    masteredCardCount = masteredCards,
                    totalCardCount = totalCards,
                    detectedEpochMs = detectedEpochMs,
                    confirmedEpochMs = confirmedEpochMs
                )
            )
        }

        // Return deterministic sort: highest mastery rate first, then by topicCode for stability
        return result.sortedWith(
            compareByDescending<LearningStrength> { it.masteryRate }
                .thenByDescending { it.averageRetention }
                .thenBy { it.topicCode }
        )
    }
}
