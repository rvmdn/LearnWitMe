package com.learnwitme.core.domain.model.memory.engine

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.analyzer.LearningMemoryAnalysisResult

// =====================================================================
// Learning Memory — LearningMemoryEngineResult
// Sprint 9 · Stage 3 · Learning Memory Update Engine
//
// Responsibility: The single, immutable output of the Update Engine.
// Bundles the updated LearningMemory aggregate with the full audit
// trail of every individual update that was applied.
//
// This is the canonical handoff from the Update Engine to the caller
// (the application-level Memory Engine or a use case).
//
// The caller then:
//   1. Persists the updatedMemory via LearningMemoryRepository
//   2. Optionally logs/audits the appliedUpdates
//   3. Optionally triggers a snapshot via takeSnapshot()
// =====================================================================

/**
 * Responsibility: Immutable result of the [LearningMemoryUpdateEngine].
 *
 * Contains:
 * - The updated [LearningMemory] aggregate (new state, not yet persisted)
 * - The ordered list of [LearningMemoryUpdate] records that were applied
 * - A reference to the [LearningMemoryAnalysisResult] that drove the updates
 * - Summary metrics for diagnostics
 *
 * Invariants:
 * - [computedEpochMs] must be positive.
 * - [appliedUpdates] may be empty (if policy caused all updates to be suppressed).
 *
 * @param updatedMemory      The new, immutable LearningMemory state after all updates.
 * @param appliedUpdates     Ordered list of individual updates applied in this cycle.
 * @param analysisResult     The analysis result that was the source of these updates.
 * @param policy             The policy that governed the update decisions.
 * @param computedEpochMs    Epoch timestamp (ms) when the engine completed this computation.
 */
data class LearningMemoryEngineResult(
    val updatedMemory: LearningMemory,
    val appliedUpdates: List<LearningMemoryUpdate>,
    val analysisResult: LearningMemoryAnalysisResult,
    val policy: LearningMemoryUpdatePolicy,
    val computedEpochMs: Long
) : ValueObject {

    init {
        require(computedEpochMs > 0) {
            "Computed epoch must be positive (got $computedEpochMs)"
        }
    }

    /** The number of meaningful updates applied in this cycle. */
    val meaningfulUpdateCount: Int
        get() = appliedUpdates.count { it.isMeaningfulChange }

    /** True if any update was applied (preference, pattern, strength, weakness, or behavior). */
    val hasAnyChange: Boolean get() = meaningfulUpdateCount > 0

    /** True if any strength was added or confirmed in this cycle. */
    val hasStrengthChange: Boolean get() = appliedUpdates.any { it.affectsStrength }

    /** True if any weakness was added, updated, worsened, or resolved in this cycle. */
    val hasWeaknessChange: Boolean get() = appliedUpdates.any { it.affectsWeakness }

    /** True if any behavior was added or updated in this cycle. */
    val hasBehaviorChange: Boolean get() = appliedUpdates.any { it.affectsBehavior }

    /** True if the preference was updated in this cycle. */
    val hasPreferenceChange: Boolean
        get() = appliedUpdates.any {
            it.type == LearningMemoryUpdateType.PREFERENCE_UPDATED ||
                    it.type == LearningMemoryUpdateType.PREFERENCE_DETERMINED
        }

    /** True if the pattern was updated in this cycle. */
    val hasPatternChange: Boolean
        get() = appliedUpdates.any { it.type == LearningMemoryUpdateType.PATTERN_UPDATED }

    /** Returns a diagnostic summary of this engine result. */
    fun toSummary(): String = buildString {
        appendLine("LearningMemoryEngineResult [epochMs=$computedEpochMs]")
        appendLine("  Total updates     : ${appliedUpdates.size}")
        appendLine("  Meaningful changes: $meaningfulUpdateCount")
        appendLine("  Preference change : $hasPreferenceChange")
        appendLine("  Pattern change    : $hasPatternChange")
        appendLine("  Strength changes  : $hasStrengthChange")
        appendLine("  Weakness changes  : $hasWeaknessChange")
        appendLine("  Behavior changes  : $hasBehaviorChange")
        appendLine("  Policy used       : ${policy::class.simpleName}")
        appendLine("  Applied updates   :")
        for (update in appliedUpdates) {
            appendLine("    [${update.type}] ${update.reason}")
        }
    }
}
