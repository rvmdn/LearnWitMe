package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.LearningMemory

// =====================================================================
// Learning Memory — LearningMemoryAnalyzer
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Orchestrates all five dimension analyzers and
// produces the complete LearningMemoryAnalysisResult.
//
// This is the single entry point for the full analysis pipeline.
// It composes:
//   • LearningPreferenceAnalyzer  → infers learning style
//   • LearningPatternAnalyzer     → detects study habits
//   • LearningStrengthAnalyzer    → finds mastered topics
//   • LearningWeaknessAnalyzer    → finds struggling topics
//   • LearningBehaviorAnalyzer    → finds recurring behaviors
//
// After computing all dimensions, it performs cross-cutting
// reconciliation:
//   • Resolves weaknesses that have recovered (now above threshold)
//   • Flags strengths that have regressed (now below threshold)
//   • Ensures strength/weakness mutual exclusion at the result level
//
// NO persistence. NO side effects. NO repository access.
// The result is a pure computation — a proposal to the Memory Engine.
//
// Architecture contract:
//   LearningMemory (current state)
//   + MemoryAnalysisInput (fresh session + record data)
//   ↓
//   LearningMemoryAnalyzer.analyze(...)
//   ↓
//   LearningMemoryAnalysisResult (immutable proposal)
//   ↓
//   Memory Engine applies result to LearningMemory aggregate
//   ↓
//   Repository persists updated LearningMemory
//
// Determinism guarantee:
//   Same currentMemory + Same input → Same LearningMemoryAnalysisResult.
// =====================================================================

/**
 * Responsibility: The single orchestrating analyzer that coordinates
 * all five dimension analyzers and produces a complete, coherent
 * [LearningMemoryAnalysisResult].
 *
 * Fully stateless — no mutable instance state.
 */
object LearningMemoryAnalyzer {

    /**
     * Runs the full analysis pipeline against the learner's current memory state
     * and fresh observable session data.
     *
     * @param currentMemory The current [LearningMemory] aggregate (read-only).
     * @param input         The freshly assembled [MemoryAnalysisInput] for this cycle.
     * @return A [LearningMemoryAnalysisResult] containing all computed updates.
     *         This result does NOT modify [currentMemory] — that is the Memory Engine's job.
     */
    fun analyze(
        currentMemory: LearningMemory,
        input: MemoryAnalysisInput
    ): LearningMemoryAnalysisResult {

        // ── 1. Preference Analysis ─────────────────────────────────────
        val updatedPreference = LearningPreferenceAnalyzer.analyze(
            input = input,
            existingPreference = currentMemory.preference
        )

        // ── 2. Pattern Analysis ────────────────────────────────────────
        val updatedPattern = LearningPatternAnalyzer.analyze(input = input)

        // ── 3. Strength Analysis ───────────────────────────────────────
        val computedStrengths = LearningStrengthAnalyzer.analyze(
            input = input,
            existingStrengths = currentMemory.strengths
        )

        // ── 4. Weakness Analysis ───────────────────────────────────────
        val computedWeaknesses = LearningWeaknessAnalyzer.analyze(
            input = input,
            existingWeaknesses = currentMemory.weaknesses
        )

        // ── 5. Behavior Analysis ───────────────────────────────────────
        val computedBehaviors = LearningBehaviorAnalyzer.analyze(
            input = input,
            existingBehaviors = currentMemory.behaviors
        )

        // ── 6. Cross-Cutting Reconciliation ───────────────────────────

        // Computed strength topic codes
        val computedStrengthCodes = computedStrengths.map { it.topicCode }.toSet()
        // Computed weakness topic codes
        val computedWeaknessCodes = computedWeaknesses.map { it.topicCode }.toSet()

        // Regressed strengths: topics previously tracked as strengths that
        // no longer qualify AND do not appear in weaknesses either.
        // (If they dropped to weakness, they appear in newWeaknesses instead.)
        val regressedStrengths = currentMemory.strengths
            .map { it.topicCode }
            .filter { code -> code !in computedStrengthCodes }

        // Resolved weaknesses: topics previously tracked as weaknesses that
        // no longer appear in the computed weakness list.
        val resolvedWeaknesses = currentMemory.weaknesses
            .map { it.topicCode }
            .filter { code -> code !in computedWeaknessCodes }

        // Final mutual exclusion guard at the result level:
        // If a topic somehow appears in both (edge case from independent analyzers),
        // remove it from newStrengths — weaknesses take priority for safety.
        val overlapCodes = computedStrengthCodes.intersect(computedWeaknessCodes)
        val sanitizedStrengths = if (overlapCodes.isEmpty()) {
            computedStrengths
        } else {
            computedStrengths.filter { it.topicCode !in overlapCodes }
        }

        // ── 7. Overall Retention Average ──────────────────────────────
        val overallRetentionAverage = if (input.memoryRecords.isEmpty()) 0.0
        else input.memoryRecords.map { it.retention }.average().coerceIn(0.0, 1.0)

        return LearningMemoryAnalysisResult(
            preference = updatedPreference,
            pattern = updatedPattern,
            newStrengths = sanitizedStrengths,
            newWeaknesses = computedWeaknesses,
            resolvedWeaknesses = resolvedWeaknesses,
            regressedStrengths = regressedStrengths,
            detectedBehaviors = computedBehaviors,
            analyzedEpochMs = input.nowEpochMs,
            totalSessionsAnalyzed = input.totalSessionCount,
            totalCardsAnalyzed = input.totalCardsTracked,
            overallRetentionAverage = overallRetentionAverage
        )
    }
}
