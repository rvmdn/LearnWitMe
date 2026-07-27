package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness

// =====================================================================
// Learning Memory — LearningMemoryAnalysisResult
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Aggregates all individual analyzer outputs into a
// single, immutable, coherent result value object.
//
// This is the canonical output of the analysis pipeline.
// It is consumed by the Memory Engine to update the LearningMemory
// aggregate with fresh values.
//
// The result itself does NOT persist anything. It is a pure computation
// output — a proposal to the Memory Engine.
//
// Architecture:
//   LearningPreferenceAnalyzer ─────────┐
//   LearningPatternAnalyzer    ─────────┤
//   LearningStrengthAnalyzer   ─────────┼─→ LearningMemoryAnalysisResult
//   LearningWeaknessAnalyzer   ─────────┤
//   LearningBehaviorAnalyzer   ─────────┘
// =====================================================================

/**
 * Responsibility: Immutable aggregate output of the full Learning Memory
 * analysis pipeline. Contains the computed values from each analyzer.
 *
 * This value object is produced by [LearningMemoryAnalyzer] and is the
 * only mechanism through which analyzers communicate results to the
 * Memory Engine.
 *
 * Invariants:
 * - [analyzedEpochMs] must be positive.
 * - [totalSessionsAnalyzed] must be non-negative.
 * - [totalCardsAnalyzed] must be non-negative.
 * - A topic cannot appear in both [newStrengths] and [newWeaknesses].
 *
 * @param preference            Updated learning style preference inferred from sessions.
 * @param pattern               Updated behavioral study pattern computed from sessions.
 * @param newStrengths          Topics newly detected or re-confirmed as strengths.
 * @param newWeaknesses         Topics newly detected or worsened as weaknesses.
 * @param resolvedWeaknesses    Topic codes that no longer qualify as weaknesses (recovered).
 * @param regressedStrengths    Topic codes that no longer qualify as strengths (declined).
 * @param detectedBehaviors     Observable behaviors detected across sessions.
 * @param analyzedEpochMs       Epoch timestamp (ms) when this analysis was computed.
 * @param totalSessionsAnalyzed Number of sessions that were input to this analysis.
 * @param totalCardsAnalyzed    Number of memory records that were input to this analysis.
 * @param overallRetentionAverage Mean retention score across all analyzed records.
 */
data class LearningMemoryAnalysisResult(
    val preference: LearningPreference,
    val pattern: LearningPattern,
    val newStrengths: List<LearningStrength>,
    val newWeaknesses: List<LearningWeakness>,
    val resolvedWeaknesses: List<String>,
    val regressedStrengths: List<String>,
    val detectedBehaviors: List<LearningBehavior>,
    val analyzedEpochMs: Long,
    val totalSessionsAnalyzed: Int,
    val totalCardsAnalyzed: Int,
    val overallRetentionAverage: Double
) : ValueObject {

    init {
        require(analyzedEpochMs > 0) {
            "Analyzed epoch must be positive (got $analyzedEpochMs)"
        }
        require(totalSessionsAnalyzed >= 0) {
            "Total sessions analyzed cannot be negative (got $totalSessionsAnalyzed)"
        }
        require(totalCardsAnalyzed >= 0) {
            "Total cards analyzed cannot be negative (got $totalCardsAnalyzed)"
        }
        require(overallRetentionAverage in 0.0..1.0) {
            "Overall retention average must be in [0.0, 1.0] (got $overallRetentionAverage)"
        }

        // Invariant: A topic cannot be simultaneously a strength and a weakness
        val strengthTopics = newStrengths.map { it.topicCode }.toSet()
        val weaknessTopics = newWeaknesses.map { it.topicCode }.toSet()
        val overlap = strengthTopics.intersect(weaknessTopics)
        require(overlap.isEmpty()) {
            "Analysis result cannot contain a topic as both Strength and Weakness: $overlap"
        }
    }

    /** True if any new strengths were detected in this analysis cycle. */
    val hasNewStrengths: Boolean get() = newStrengths.isNotEmpty()

    /** True if any new or worsened weaknesses were detected. */
    val hasNewWeaknesses: Boolean get() = newWeaknesses.isNotEmpty()

    /** True if any previously tracked weaknesses have been resolved. */
    val hasResolvedWeaknesses: Boolean get() = resolvedWeaknesses.isNotEmpty()

    /** True if any previously tracked strengths have regressed. */
    val hasRegressedStrengths: Boolean get() = regressedStrengths.isNotEmpty()

    /** True if the learning preference was determined (not UNDETERMINED). */
    val preferenceIsDetermined: Boolean get() = preference.isDetermined

    /** True if at least one severe weakness was detected. */
    val hasSevereWeakness: Boolean get() =
        newWeaknesses.any { it.isCritical }

    /** Returns a summary for logging and diagnostics. */
    fun toSummary(): String = buildString {
        appendLine("LearningMemoryAnalysisResult [epochMs=$analyzedEpochMs]")
        appendLine("  Sessions analyzed : $totalSessionsAnalyzed")
        appendLine("  Cards analyzed    : $totalCardsAnalyzed")
        appendLine("  Overall retention : ${"%.2f".format(overallRetentionAverage)}")
        appendLine("  Preference        : ${preference.primaryStyle} (confidence=${"%.2f".format(preference.confidenceScore)})")
        appendLine("  Study window      : ${pattern.bestStudyWindow}")
        appendLine("  New strengths     : ${newStrengths.map { it.topicCode }}")
        appendLine("  New weaknesses    : ${newWeaknesses.map { "${it.topicCode}(${it.severity})" }}")
        appendLine("  Resolved weak.    : $resolvedWeaknesses")
        appendLine("  Regressed str.    : $regressedStrengths")
        appendLine("  Behaviors         : ${detectedBehaviors.map { it.type }}")
    }
}
