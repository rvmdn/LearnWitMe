package com.learnwitme.core.domain.model.memory.engine

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import com.learnwitme.core.domain.model.memory.analyzer.LearningMemoryAnalysisResult

// =====================================================================
// Learning Memory — LearningMemoryUpdateEngine
// Sprint 9 · Stage 3 · Learning Memory Update Engine
//
// Responsibility: Applies a LearningMemoryAnalysisResult to the current
// LearningMemory aggregate, producing:
//   • An ordered list of LearningMemoryUpdate records (the audit trail)
//   • The updated, immutable LearningMemory (the new aggregate state)
//
// The engine is policy-driven: all update decisions are governed
// by the LearningMemoryUpdatePolicy provided at call time.
//
// Engine guarantees:
//   • No repository access
//   • No side effects
//   • No mutable global state
//   • Same inputs + same policy → same outputs
//
// Processing order (applied sequentially to preserve consistency):
//   1. Preference
//   2. Pattern
//   3. Resolved weaknesses   (removals first — clears path for strengths)
//   4. Regressed strengths   (removals)
//   5. New strengths         (additions/confirmations)
//   6. New weaknesses        (additions/updates)
//   7. Behaviors             (additions/updates, then pruning)
//
// The order matters: removing a weakness before adding a strength for
// the same topic satisfies LearningMemory's mutual-exclusion invariant.
// =====================================================================

/**
 * Responsibility: Stateless engine that applies a [LearningMemoryAnalysisResult]
 * to an existing [LearningMemory] aggregate, governed by a [LearningMemoryUpdatePolicy].
 *
 * Produces a [LearningMemoryEngineResult] containing the updated aggregate
 * and a complete, ordered audit trail of every update decision made.
 *
 * Fully stateless — no instance fields. All state is carried through
 * the function call stack only.
 */
object LearningMemoryUpdateEngine {

    /**
     * Applies [analysisResult] to [currentMemory] using [policy] rules.
     *
     * @param currentMemory  The current immutable [LearningMemory] aggregate.
     * @param analysisResult The analysis result produced by [com.learnwitme.core.domain.model.memory.analyzer.LearningMemoryAnalyzer].
     * @param policy         The update policy governing merge decisions. Defaults to [LearningMemoryUpdatePolicy.DEFAULT].
     * @return A [LearningMemoryEngineResult] containing the updated aggregate and audit trail.
     */
    fun apply(
        currentMemory: LearningMemory,
        analysisResult: LearningMemoryAnalysisResult,
        policy: LearningMemoryUpdatePolicy = LearningMemoryUpdatePolicy.DEFAULT
    ): LearningMemoryEngineResult {

        val nowEpochMs = analysisResult.analyzedEpochMs
        val updates = mutableListOf<LearningMemoryUpdate>()
        var memory = currentMemory

        // ── Step 1: Preference ────────────────────────────────────────
        val preferenceResult = applyPreference(
            memory = memory,
            newPreference = analysisResult.preference,
            sessionsAnalyzed = analysisResult.totalSessionsAnalyzed,
            policy = policy,
            nowEpochMs = nowEpochMs
        )
        updates.add(preferenceResult.update)
        memory = preferenceResult.memory

        // ── Step 2: Pattern ───────────────────────────────────────────
        val patternResult = applyPattern(
            memory = memory,
            analysisResult = analysisResult,
            policy = policy,
            nowEpochMs = nowEpochMs
        )
        updates.add(patternResult.update)
        memory = patternResult.memory

        // ── Step 3: Resolved Weaknesses (removals first) ──────────────
        for (topicCode in analysisResult.resolvedWeaknesses) {
            val weaknessResult = applyResolvedWeakness(
                memory = memory,
                topicCode = topicCode,
                policy = policy,
                nowEpochMs = nowEpochMs
            )
            updates.add(weaknessResult.update)
            memory = weaknessResult.memory
        }

        // ── Step 4: Regressed Strengths (removals) ────────────────────
        for (topicCode in analysisResult.regressedStrengths) {
            val strengthResult = applyRegressedStrength(
                memory = memory,
                topicCode = topicCode,
                policy = policy,
                nowEpochMs = nowEpochMs
            )
            updates.add(strengthResult.update)
            memory = strengthResult.memory
        }

        // ── Step 5: New / Re-confirmed Strengths ──────────────────────
        for (strength in analysisResult.newStrengths) {
            val strengthResult = applyStrength(
                memory = memory,
                strength = strength,
                nowEpochMs = nowEpochMs
            )
            updates.add(strengthResult.update)
            memory = strengthResult.memory
        }
        // Apply cap if configured
        memory = applyStrengthCap(memory, policy)

        // ── Step 6: New / Updated Weaknesses ──────────────────────────
        for (weakness in analysisResult.newWeaknesses) {
            val weaknessResult = applyWeakness(
                memory = memory,
                weakness = weakness,
                policy = policy,
                nowEpochMs = nowEpochMs
            )
            updates.add(weaknessResult.update)
            memory = weaknessResult.memory
        }
        // Apply cap if configured
        memory = applyWeaknessCap(memory, policy)

        // ── Step 7: Behaviors ─────────────────────────────────────────
        val behaviorResults = applyBehaviors(
            memory = memory,
            detectedBehaviors = analysisResult.detectedBehaviors,
            policy = policy,
            nowEpochMs = nowEpochMs
        )
        updates.addAll(behaviorResults.updates)
        memory = behaviorResults.memory

        return LearningMemoryEngineResult(
            updatedMemory = memory,
            appliedUpdates = updates.toList(),
            analysisResult = analysisResult,
            policy = policy,
            computedEpochMs = nowEpochMs
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Step implementations
    // ─────────────────────────────────────────────────────────────────

    /**
     * Applies the preference update according to policy.
     *
     * Strategy logic:
     * - REPLACE: Always set the new preference.
     * - MERGE:   Update only if (a) session count meets the minimum AND
     *            (b) confidence delta exceeds the configured threshold OR
     *            the style type itself has changed.
     * - IGNORE:  Skip update regardless of analysis output.
     * - PRESERVE_HISTORY: Equivalent to MERGE for preference (timestamps carried).
     */
    private fun applyPreference(
        memory: LearningMemory,
        newPreference: LearningPreference,
        sessionsAnalyzed: Int,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): StepResult {
        val existing = memory.preference

        return when (policy.preferenceStrategy) {
            UpdateMergeStrategy.IGNORE -> StepResult(
                memory = memory,
                update = LearningMemoryUpdate.noChange(
                    reason = "Preference update skipped — policy is IGNORE",
                    nowEpochMs = nowEpochMs
                )
            )

            UpdateMergeStrategy.REPLACE -> {
                val updated = memory.updatePreference(newPreference)
                val wasDetermined = !existing.isDetermined && newPreference.isDetermined
                StepResult(
                    memory = updated,
                    update = LearningMemoryUpdate.preferenceUpdated(
                        newPreference = newPreference,
                        determined = wasDetermined,
                        reason = "Preference replaced by REPLACE strategy",
                        nowEpochMs = nowEpochMs
                    )
                )
            }

            UpdateMergeStrategy.MERGE, UpdateMergeStrategy.PRESERVE_HISTORY -> {
                // Guard: insufficient sessions
                if (sessionsAnalyzed < policy.minSessionsForPreferenceUpdate) {
                    return StepResult(
                        memory = memory,
                        update = LearningMemoryUpdate.noChange(
                            reason = "Preference not updated — insufficient sessions " +
                                    "($sessionsAnalyzed < ${policy.minSessionsForPreferenceUpdate})",
                            nowEpochMs = nowEpochMs
                        )
                    )
                }

                // Guard: new preference is UNDETERMINED — don't overwrite a determined preference
                if (!newPreference.isDetermined && existing.isDetermined) {
                    return StepResult(
                        memory = memory,
                        update = LearningMemoryUpdate.noChange(
                            reason = "Preference not updated — new signal is UNDETERMINED, " +
                                    "preserving existing determined preference",
                            nowEpochMs = nowEpochMs
                        )
                    )
                }

                // Guard: style unchanged AND confidence delta below threshold
                val styleUnchanged = newPreference.primaryStyle == existing.primaryStyle
                val confidenceDelta = kotlin.math.abs(
                    newPreference.confidenceScore - existing.confidenceScore
                )
                val deltaBelowThreshold =
                    confidenceDelta < policy.minConfidenceDeltaForPreferenceChange

                if (styleUnchanged && deltaBelowThreshold) {
                    return StepResult(
                        memory = memory,
                        update = LearningMemoryUpdate.noChange(
                            reason = "Preference not updated — style unchanged and confidence delta " +
                                    "${"%.3f".format(confidenceDelta)} < threshold " +
                                    "${policy.minConfidenceDeltaForPreferenceChange}",
                            nowEpochMs = nowEpochMs
                        )
                    )
                }

                // Merge: carry observation count forward
                val merged = newPreference.copy(
                    observationCount = maxOf(
                        newPreference.observationCount,
                        existing.observationCount
                    )
                )
                val wasDetermined = !existing.isDetermined && merged.isDetermined
                StepResult(
                    memory = memory.updatePreference(merged),
                    update = LearningMemoryUpdate.preferenceUpdated(
                        newPreference = merged,
                        determined = wasDetermined,
                        reason = if (wasDetermined)
                            "Learning style determined: ${merged.primaryStyle} " +
                                    "(confidence=${"%.2f".format(merged.confidenceScore)})"
                        else
                            "Preference merged — style: ${merged.primaryStyle}, " +
                                    "confidence delta: ${"%.3f".format(confidenceDelta)}",
                        nowEpochMs = nowEpochMs
                    )
                )
            }
        }
    }

    /**
     * Applies the pattern update according to policy.
     *
     * Strategy logic:
     * - REPLACE / PRESERVE_HISTORY: Always replace with the newly computed pattern.
     * - MERGE: Blend session count and duration with existing pattern.
     * - IGNORE: Skip update.
     */
    private fun applyPattern(
        memory: LearningMemory,
        analysisResult: LearningMemoryAnalysisResult,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): StepResult {
        val newPattern = analysisResult.pattern
        val existing = memory.pattern

        return when (policy.patternStrategy) {
            UpdateMergeStrategy.IGNORE -> StepResult(
                memory = memory,
                update = LearningMemoryUpdate.noChange(
                    reason = "Pattern update skipped — policy is IGNORE",
                    nowEpochMs = nowEpochMs
                )
            )

            UpdateMergeStrategy.REPLACE, UpdateMergeStrategy.PRESERVE_HISTORY -> StepResult(
                memory = memory.updatePattern(newPattern),
                update = LearningMemoryUpdate.patternUpdated(
                    newPattern = newPattern,
                    reason = "Pattern replaced — sessions: ${newPattern.sessionCount}, " +
                            "window: ${newPattern.bestStudyWindow}, " +
                            "completion: ${"%.0f".format(newPattern.completionRate * 100)}%",
                    nowEpochMs = nowEpochMs
                )
            )

            UpdateMergeStrategy.MERGE -> {
                // Merge: accumulate session count; blend average duration
                val mergedSessionCount = existing.sessionCount + newPattern.sessionCount
                val mergedDurationMs = if (mergedSessionCount > 0) {
                    (existing.averageSessionDurationMs * existing.sessionCount +
                            newPattern.averageSessionDurationMs * newPattern.sessionCount) /
                            mergedSessionCount
                } else 0L

                // Prefer the newer window and difficulty if pattern is reliable
                val mergedPattern = newPattern.copy(
                    sessionCount = mergedSessionCount,
                    averageSessionDurationMs = mergedDurationMs,
                    lastComputedEpochMs = nowEpochMs
                )
                StepResult(
                    memory = memory.updatePattern(mergedPattern),
                    update = LearningMemoryUpdate.patternUpdated(
                        newPattern = mergedPattern,
                        reason = "Pattern merged — total sessions: $mergedSessionCount, " +
                                "avg duration: ${mergedDurationMs / 60_000}min",
                        nowEpochMs = nowEpochMs
                    )
                )
            }
        }
    }

    /**
     * Removes a resolved weakness from the aggregate if policy allows.
     */
    private fun applyResolvedWeakness(
        memory: LearningMemory,
        topicCode: String,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): StepResult {
        if (!policy.removeResolvedWeaknesses) {
            return StepResult(
                memory = memory,
                update = LearningMemoryUpdate.noChange(
                    reason = "Resolved weakness for '$topicCode' retained — " +
                            "policy removeResolvedWeaknesses=false",
                    nowEpochMs = nowEpochMs
                )
            )
        }
        return StepResult(
            memory = memory.removeResolvedWeakness(topicCode),
            update = LearningMemoryUpdate.weaknessResolved(
                topicCode = topicCode,
                nowEpochMs = nowEpochMs
            )
        )
    }

    /**
     * Removes a regressed strength from the aggregate if policy allows.
     */
    private fun applyRegressedStrength(
        memory: LearningMemory,
        topicCode: String,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): StepResult {
        if (!policy.removeRegressedStrengths) {
            return StepResult(
                memory = memory,
                update = LearningMemoryUpdate.noChange(
                    reason = "Regressed strength for '$topicCode' retained — " +
                            "policy removeRegressedStrengths=false",
                    nowEpochMs = nowEpochMs
                )
            )
        }
        return StepResult(
            memory = memory.removeRegressedStrength(topicCode),
            update = LearningMemoryUpdate.strengthRemoved(
                topicCode = topicCode,
                nowEpochMs = nowEpochMs
            )
        )
    }

    /**
     * Adds or confirms a strength in the aggregate.
     * If the topic already exists in strengths, this is a re-confirmation.
     */
    private fun applyStrength(
        memory: LearningMemory,
        strength: LearningStrength,
        nowEpochMs: Long
    ): StepResult {
        val isExisting = memory.strengths.any { it.topicCode == strength.topicCode }
        val updated = memory.recordStrength(strength)
        val update = if (isExisting)
            LearningMemoryUpdate.strengthConfirmed(strength, nowEpochMs)
        else
            LearningMemoryUpdate.strengthAdded(strength, nowEpochMs)
        return StepResult(memory = updated, update = update)
    }

    /**
     * Adds or updates a weakness in the aggregate.
     * Determines update type by comparing severity with existing entry.
     */
    private fun applyWeakness(
        memory: LearningMemory,
        weakness: LearningWeakness,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): StepResult {
        val existing = memory.weaknesses.find { it.topicCode == weakness.topicCode }
        val updated = memory.recordWeakness(weakness)

        val update = when {
            existing == null ->
                LearningMemoryUpdate.weaknessAdded(weakness, nowEpochMs)

            severityOrdinal(weakness.severity) > severityOrdinal(existing.severity) &&
                    severityOrdinal(weakness.severity) >=
                    severityOrdinal(policy.weaknessSeverityEscalationThreshold) ->
                LearningMemoryUpdate.weaknessWorsened(weakness, nowEpochMs)

            else ->
                LearningMemoryUpdate.weaknessUpdated(weakness, nowEpochMs)
        }
        return StepResult(memory = updated, update = update)
    }

    /**
     * Applies behavior detections to the aggregate.
     * Respects policy.preserveBehaviorsIfNotRedetected.
     * Prunes behaviors to maxBehaviorsRetained after applying.
     */
    private fun applyBehaviors(
        memory: LearningMemory,
        detectedBehaviors: List<LearningBehavior>,
        policy: LearningMemoryUpdatePolicy,
        nowEpochMs: Long
    ): BehaviorStepResult {
        val updates = mutableListOf<LearningMemoryUpdate>()
        var currentMemory = memory

        // Apply each detected behavior
        for (behavior in detectedBehaviors) {
            val isExisting = currentMemory.behaviors.any { it.type == behavior.type }
            currentMemory = currentMemory.recordBehavior(behavior)
            updates.add(
                if (isExisting) LearningMemoryUpdate.behaviorUpdated(behavior, nowEpochMs)
                else LearningMemoryUpdate.behaviorAdded(behavior, nowEpochMs)
            )
        }

        // If not preserving, remove behaviors that were not re-detected
        if (!policy.preserveBehaviorsIfNotRedetected) {
            val detectedTypes = detectedBehaviors.map { it.type }.toSet()
            val behaviorsBefore = currentMemory.behaviors
            val behaviorsAfter = behaviorsBefore.filter { it.type in detectedTypes }
            if (behaviorsAfter.size < behaviorsBefore.size) {
                currentMemory = currentMemory.copy(behaviors = behaviorsAfter)
            }
        }

        // Apply cap
        if (policy.maxBehaviorsRetained > 0 &&
            currentMemory.behaviors.size > policy.maxBehaviorsRetained
        ) {
            val trimmed = currentMemory.behaviors
                .sortedByDescending { it.lastObservedEpochMs }
                .take(policy.maxBehaviorsRetained)
            currentMemory = currentMemory.copy(behaviors = trimmed)
        }

        return BehaviorStepResult(memory = currentMemory, updates = updates.toList())
    }

    /**
     * Applies the max strengths cap if configured (maxStrengthsRetained > 0).
     * Keeps the strengths with the highest mastery rate when pruning.
     */
    private fun applyStrengthCap(
        memory: LearningMemory,
        policy: LearningMemoryUpdatePolicy
    ): LearningMemory {
        if (policy.maxStrengthsRetained <= 0) return memory
        if (memory.strengths.size <= policy.maxStrengthsRetained) return memory
        val kept = memory.strengths
            .sortedByDescending { it.masteryRate }
            .take(policy.maxStrengthsRetained)
        return memory.copy(strengths = kept)
    }

    /**
     * Applies the max weaknesses cap if configured (maxWeaknessesRetained > 0).
     * Keeps the weaknesses with the lowest retention (most critical) when pruning.
     */
    private fun applyWeaknessCap(
        memory: LearningMemory,
        policy: LearningMemoryUpdatePolicy
    ): LearningMemory {
        if (policy.maxWeaknessesRetained <= 0) return memory
        if (memory.weaknesses.size <= policy.maxWeaknessesRetained) return memory
        val kept = memory.weaknesses
            .sortedWith(
                compareByDescending<LearningWeakness> { it.severity.ordinal }
                    .thenBy { it.averageRetention }
            )
            .take(policy.maxWeaknessesRetained)
        return memory.copy(weaknesses = kept)
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────

    /** Comparable ordinal for WeaknessSeverity (MILD=0, MODERATE=1, SEVERE=2). */
    private fun severityOrdinal(severity: WeaknessSeverity): Int = severity.ordinal

    /** Carries memory + single update through a step. */
    private data class StepResult(
        val memory: LearningMemory,
        val update: LearningMemoryUpdate
    )

    /** Carries memory + multiple updates through a behavior step. */
    private data class BehaviorStepResult(
        val memory: LearningMemory,
        val updates: List<LearningMemoryUpdate>
    )
}
