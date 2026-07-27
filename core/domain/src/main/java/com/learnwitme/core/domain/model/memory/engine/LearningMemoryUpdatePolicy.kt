package com.learnwitme.core.domain.model.memory.engine

import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.WeaknessSeverity

// =====================================================================
// Learning Memory — LearningMemoryUpdatePolicy
// Sprint 9 · Stage 3 · Learning Memory Update Engine
//
// Responsibility: Defines the set of configurable rules that govern
// how the Update Engine applies each analysis dimension to the
// current LearningMemory aggregate.
//
// The policy is pure configuration — it carries no logic itself.
// All decision logic lives in LearningMemoryUpdateEngine.
//
// Policy rules control:
//   • How the preference is updated (replace vs. merge vs. ignore)
//   • How the pattern is updated (replace vs. merge vs. ignore)
//   • Minimum sessions required before preference is updated
//   • Minimum confidence delta to trigger a preference change
//   • Whether behaviors are preserved if not re-detected
//   • Severity threshold for weakness escalation to be flagged
//   • Whether resolved weaknesses are removed or retained as history
//
// Determinism guarantee:
//   Same policy + same inputs → same output.
// =====================================================================

/**
 * Defines how the Update Engine resolves a conflict between an existing
 * value in the aggregate and the newly analyzed value.
 */
enum class UpdateMergeStrategy {
    /**
     * The new value completely replaces the existing value.
     * No history of the old value is preserved in the aggregate.
     */
    REPLACE,

    /**
     * The new value is merged with the existing value, preserving cumulative
     * signals (e.g., observation counts, timestamps).
     * The new computed value takes precedence for primary fields.
     */
    MERGE,

    /**
     * The existing value is kept unchanged. The new analysis result is ignored
     * for this dimension in this update cycle.
     * Used when the analyzer did not produce a reliable signal.
     */
    IGNORE,

    /**
     * The existing value is updated but history is preserved in the aggregate
     * snapshot mechanism (e.g., strength detection timestamp is preserved).
     */
    PRESERVE_HISTORY
}

/**
 * Responsibility: Encapsulates the configurable rules that govern
 * how the [LearningMemoryUpdateEngine] applies analysis results
 * to a [com.learnwitme.core.domain.model.LearningMemory] aggregate.
 *
 * All fields have sensible production defaults. The policy object
 * is immutable and value-object typed — it can be safely shared.
 *
 * Invariants:
 * - [minSessionsForPreferenceUpdate] must be non-negative.
 * - [minConfidenceDeltaForPreferenceChange] must be in [0.0, 1.0].
 * - [weaknessSeverityEscalationThreshold] must be a valid [WeaknessSeverity].
 *
 * @param preferenceStrategy              How to merge a newly analyzed preference. Default: MERGE.
 * @param patternStrategy                 How to merge a newly analyzed pattern. Default: REPLACE.
 * @param minSessionsForPreferenceUpdate  Minimum sessions analyzed before preference is allowed to update. Default: 3.
 * @param minConfidenceDeltaForPreferenceChange Minimum delta between old and new confidence before updating. Default: 0.05.
 * @param preserveBehaviorsIfNotRedetected If true, existing behaviors not detected in this cycle are kept. Default: true.
 * @param maxBehaviorsRetained            Maximum number of behavior entries to retain in the aggregate. Default: 10.
 * @param removeResolvedWeaknesses        If true, recovered weaknesses are removed from the aggregate. Default: true.
 * @param weaknessSeverityEscalationThreshold Minimum severity at which a weakness update is classified as WORSENED vs UPDATED. Default: MODERATE.
 * @param removeRegressedStrengths        If true, declined strengths are removed from the aggregate. Default: true.
 * @param maxStrengthsRetained            Maximum number of strength entries retained. 0 means unlimited. Default: 0.
 * @param maxWeaknessesRetained           Maximum number of weakness entries retained. 0 means unlimited. Default: 0.
 */
data class LearningMemoryUpdatePolicy(
    val preferenceStrategy: UpdateMergeStrategy = UpdateMergeStrategy.MERGE,
    val patternStrategy: UpdateMergeStrategy = UpdateMergeStrategy.REPLACE,
    val minSessionsForPreferenceUpdate: Int = 3,
    val minConfidenceDeltaForPreferenceChange: Double = 0.05,
    val preserveBehaviorsIfNotRedetected: Boolean = true,
    val maxBehaviorsRetained: Int = 10,
    val removeResolvedWeaknesses: Boolean = true,
    val weaknessSeverityEscalationThreshold: WeaknessSeverity = WeaknessSeverity.MODERATE,
    val removeRegressedStrengths: Boolean = true,
    val maxStrengthsRetained: Int = 0,
    val maxWeaknessesRetained: Int = 0
) : ValueObject {

    init {
        require(minSessionsForPreferenceUpdate >= 0) {
            "minSessionsForPreferenceUpdate cannot be negative (got $minSessionsForPreferenceUpdate)"
        }
        require(minConfidenceDeltaForPreferenceChange in 0.0..1.0) {
            "minConfidenceDeltaForPreferenceChange must be in [0.0, 1.0] (got $minConfidenceDeltaForPreferenceChange)"
        }
        require(maxBehaviorsRetained >= 0) {
            "maxBehaviorsRetained cannot be negative (got $maxBehaviorsRetained)"
        }
        require(maxStrengthsRetained >= 0) {
            "maxStrengthsRetained cannot be negative (got $maxStrengthsRetained)"
        }
        require(maxWeaknessesRetained >= 0) {
            "maxWeaknessesRetained cannot be negative (got $maxWeaknessesRetained)"
        }
    }

    companion object {
        /** Production default policy. Suitable for most learner profiles. */
        val DEFAULT: LearningMemoryUpdatePolicy = LearningMemoryUpdatePolicy()

        /** Conservative policy: prefer stability, require stronger signal before updating. */
        val CONSERVATIVE: LearningMemoryUpdatePolicy = LearningMemoryUpdatePolicy(
            preferenceStrategy = UpdateMergeStrategy.MERGE,
            patternStrategy = UpdateMergeStrategy.MERGE,
            minSessionsForPreferenceUpdate = 5,
            minConfidenceDeltaForPreferenceChange = 0.10,
            preserveBehaviorsIfNotRedetected = true,
            removeResolvedWeaknesses = true,
            weaknessSeverityEscalationThreshold = WeaknessSeverity.SEVERE
        )

        /** Aggressive policy: always replace with fresh analysis, no signal threshold. */
        val AGGRESSIVE: LearningMemoryUpdatePolicy = LearningMemoryUpdatePolicy(
            preferenceStrategy = UpdateMergeStrategy.REPLACE,
            patternStrategy = UpdateMergeStrategy.REPLACE,
            minSessionsForPreferenceUpdate = 1,
            minConfidenceDeltaForPreferenceChange = 0.0,
            preserveBehaviorsIfNotRedetected = false,
            removeResolvedWeaknesses = true,
            weaknessSeverityEscalationThreshold = WeaknessSeverity.MILD
        )
    }
}
