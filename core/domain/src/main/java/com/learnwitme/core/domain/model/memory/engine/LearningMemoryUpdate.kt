package com.learnwitme.core.domain.model.memory.engine

import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness

// =====================================================================
// Learning Memory — LearningMemoryUpdateType & LearningMemoryUpdate
// Sprint 9 · Stage 3 · Learning Memory Update Engine
//
// Responsibility: Defines the complete taxonomy of deterministic changes
// that can be applied to a LearningMemory aggregate, and the immutable
// value object that represents a single batch of such changes.
//
// A LearningMemoryUpdate is:
//   • A record of WHAT changed (the type)
//   • A carrier of the new value (the payload — sealed per type)
//   • Produced by the Update Engine; consumed by the caller
//
// LearningMemoryUpdate is NOT applied itself — the Update Engine
// applies it to the aggregate and returns the updated LearningMemory.
//
// Architecture position:
//   AnalysisResult → Update Engine → LearningMemoryUpdate + LearningMemory
// =====================================================================

/**
 * Classifies the type of a deterministic update applied to a [com.learnwitme.core.domain.model.LearningMemory].
 *
 * Each variant maps to exactly one category of change in the aggregate.
 * The grouping reflects the five Sprint 9 profile dimensions plus the
 * two lifecycle reconciliation operations.
 */
enum class LearningMemoryUpdateType {

    // ── Preference Dimension ──────────────────────────────────────────
    /** The inferred learning style preference was updated. */
    PREFERENCE_UPDATED,

    /** The learning style was replaced with a newly determined style. */
    PREFERENCE_DETERMINED,

    // ── Pattern Dimension ─────────────────────────────────────────────
    /** Behavioral study pattern was recomputed from new session data. */
    PATTERN_UPDATED,

    // ── Strength Dimension ────────────────────────────────────────────
    /** A new topic mastery was detected and added. */
    STRENGTH_ADDED,

    /** An existing topic mastery was re-confirmed and refreshed. */
    STRENGTH_CONFIRMED,

    /** A previously mastered topic has regressed below threshold. */
    STRENGTH_REMOVED,

    // ── Weakness Dimension ────────────────────────────────────────────
    /** A new struggling topic was detected and added. */
    WEAKNESS_ADDED,

    /** An existing weakness worsened (severity increased). */
    WEAKNESS_WORSENED,

    /** An existing weakness improved (severity decreased or stable). */
    WEAKNESS_UPDATED,

    /** A previously tracked weakness is resolved (topic has recovered). */
    WEAKNESS_RESOLVED,

    // ── Behavior Dimension ────────────────────────────────────────────
    /** A new recurring behavior was detected and added. */
    BEHAVIOR_ADDED,

    /** An existing behavior observation was refreshed with new evidence. */
    BEHAVIOR_UPDATED,

    // ── No-Op ─────────────────────────────────────────────────────────
    /** Analysis produced no meaningful change for this dimension. */
    NO_CHANGE
}

/**
 * Responsibility: Represents a single, immutable, deterministic update
 * operation for one dimension of the [com.learnwitme.core.domain.model.LearningMemory] aggregate.
 *
 * Each [LearningMemoryUpdate] captures:
 * - The [type] of update that occurred.
 * - Optional payload fields containing the new value(s) for that update type.
 * - A human-readable [reason] for traceability and diagnostics.
 * - The [appliedEpochMs] timestamp for audit purposes.
 *
 * Invariants:
 * - [appliedEpochMs] must be positive.
 * - [reason] must not be blank.
 * - Payload fields are type-specific — see individual [LearningMemoryUpdateType] docs.
 *
 * @param type              The category of change applied.
 * @param reason            Human-readable explanation of why this update was applied.
 * @param appliedEpochMs    Epoch timestamp (ms) when this update was computed.
 * @param updatedPreference Set when type is [LearningMemoryUpdateType.PREFERENCE_UPDATED] or [LearningMemoryUpdateType.PREFERENCE_DETERMINED].
 * @param updatedPattern    Set when type is [LearningMemoryUpdateType.PATTERN_UPDATED].
 * @param strength          Set when type involves a Strength dimension change.
 * @param weakness          Set when type involves a Weakness dimension change.
 * @param behavior          Set when type involves a Behavior dimension change.
 * @param topicCode         Set for removal operations where only the topic code is needed.
 */
data class LearningMemoryUpdate(
    val type: LearningMemoryUpdateType,
    val reason: String,
    val appliedEpochMs: Long,
    val updatedPreference: LearningPreference? = null,
    val updatedPattern: LearningPattern? = null,
    val strength: LearningStrength? = null,
    val weakness: LearningWeakness? = null,
    val behavior: LearningBehavior? = null,
    val topicCode: String? = null
) : ValueObject {

    init {
        require(reason.isNotBlank()) { "Update reason cannot be blank" }
        require(appliedEpochMs > 0) {
            "Applied epoch must be positive (got $appliedEpochMs)"
        }
    }

    /** Returns true if this update represents a meaningful change (not NO_CHANGE). */
    val isMeaningfulChange: Boolean get() = type != LearningMemoryUpdateType.NO_CHANGE

    /** Returns true if this update affects the Strength dimension. */
    val affectsStrength: Boolean
        get() = type == LearningMemoryUpdateType.STRENGTH_ADDED ||
                type == LearningMemoryUpdateType.STRENGTH_CONFIRMED ||
                type == LearningMemoryUpdateType.STRENGTH_REMOVED

    /** Returns true if this update affects the Weakness dimension. */
    val affectsWeakness: Boolean
        get() = type == LearningMemoryUpdateType.WEAKNESS_ADDED ||
                type == LearningMemoryUpdateType.WEAKNESS_WORSENED ||
                type == LearningMemoryUpdateType.WEAKNESS_UPDATED ||
                type == LearningMemoryUpdateType.WEAKNESS_RESOLVED

    /** Returns true if this update affects the Behavior dimension. */
    val affectsBehavior: Boolean
        get() = type == LearningMemoryUpdateType.BEHAVIOR_ADDED ||
                type == LearningMemoryUpdateType.BEHAVIOR_UPDATED

    companion object {

        /** Factory: Creates a NO_CHANGE update for a given dimension. */
        fun noChange(reason: String, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.NO_CHANGE,
                reason = reason,
                appliedEpochMs = nowEpochMs
            )

        /** Factory: Creates a preference update. */
        fun preferenceUpdated(
            newPreference: LearningPreference,
            determined: Boolean,
            reason: String,
            nowEpochMs: Long
        ): LearningMemoryUpdate = LearningMemoryUpdate(
            type = if (determined) LearningMemoryUpdateType.PREFERENCE_DETERMINED
                   else LearningMemoryUpdateType.PREFERENCE_UPDATED,
            reason = reason,
            appliedEpochMs = nowEpochMs,
            updatedPreference = newPreference
        )

        /** Factory: Creates a pattern update. */
        fun patternUpdated(
            newPattern: LearningPattern,
            reason: String,
            nowEpochMs: Long
        ): LearningMemoryUpdate = LearningMemoryUpdate(
            type = LearningMemoryUpdateType.PATTERN_UPDATED,
            reason = reason,
            appliedEpochMs = nowEpochMs,
            updatedPattern = newPattern
        )

        /** Factory: Creates a strength-added update. */
        fun strengthAdded(strength: LearningStrength, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.STRENGTH_ADDED,
                reason = "New mastery detected for topic '${strength.topicCode}'",
                appliedEpochMs = nowEpochMs,
                strength = strength
            )

        /** Factory: Creates a strength-confirmed update. */
        fun strengthConfirmed(strength: LearningStrength, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.STRENGTH_CONFIRMED,
                reason = "Mastery re-confirmed for topic '${strength.topicCode}'",
                appliedEpochMs = nowEpochMs,
                strength = strength
            )

        /** Factory: Creates a strength-removed update. */
        fun strengthRemoved(topicCode: String, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.STRENGTH_REMOVED,
                reason = "Topic '$topicCode' has regressed below mastery threshold",
                appliedEpochMs = nowEpochMs,
                topicCode = topicCode
            )

        /** Factory: Creates a weakness-added update. */
        fun weaknessAdded(weakness: LearningWeakness, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.WEAKNESS_ADDED,
                reason = "New struggle detected for topic '${weakness.topicCode}' (${weakness.severity})",
                appliedEpochMs = nowEpochMs,
                weakness = weakness
            )

        /** Factory: Creates a weakness-worsened update. */
        fun weaknessWorsened(weakness: LearningWeakness, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.WEAKNESS_WORSENED,
                reason = "Weakness worsened for topic '${weakness.topicCode}' → ${weakness.severity}",
                appliedEpochMs = nowEpochMs,
                weakness = weakness
            )

        /** Factory: Creates a weakness-updated (stable or improved) update. */
        fun weaknessUpdated(weakness: LearningWeakness, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.WEAKNESS_UPDATED,
                reason = "Weakness refreshed for topic '${weakness.topicCode}' (${weakness.severity})",
                appliedEpochMs = nowEpochMs,
                weakness = weakness
            )

        /** Factory: Creates a weakness-resolved update. */
        fun weaknessResolved(topicCode: String, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.WEAKNESS_RESOLVED,
                reason = "Topic '$topicCode' has recovered — weakness resolved",
                appliedEpochMs = nowEpochMs,
                topicCode = topicCode
            )

        /** Factory: Creates a behavior-added update. */
        fun behaviorAdded(behavior: LearningBehavior, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.BEHAVIOR_ADDED,
                reason = "New behavior detected: ${behavior.type}",
                appliedEpochMs = nowEpochMs,
                behavior = behavior
            )

        /** Factory: Creates a behavior-updated update. */
        fun behaviorUpdated(behavior: LearningBehavior, nowEpochMs: Long): LearningMemoryUpdate =
            LearningMemoryUpdate(
                type = LearningMemoryUpdateType.BEHAVIOR_UPDATED,
                reason = "Behavior observation refreshed: ${behavior.type}",
                appliedEpochMs = nowEpochMs,
                behavior = behavior
            )
    }
}
