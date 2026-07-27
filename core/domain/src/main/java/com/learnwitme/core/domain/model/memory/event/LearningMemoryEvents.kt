package com.learnwitme.core.domain.model.memory.event

import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.LearningMemorySnapshot
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdate

// =====================================================================
// Learning Memory — Concrete Event Types
// Sprint 9 · Stage 4 · Learning Memory Events & History
//
// Responsibility: Implements every concrete Learning Memory domain event.
//
// Event hierarchy:
//   LearningMemoryEvent (sealed interface)
//   ├── LearningMemoryUpdatedEvent
//   ├── LearningMemorySnapshotCreatedEvent
//   ├── LearningMemoryPreferenceDeterminedEvent
//   ├── LearningMemoryStrengthDetectedEvent
//   ├── LearningMemorySevereWeaknessDetectedEvent
//   └── LearningMemoryWeaknessResolvedEvent
//
// All events are immutable data classes implementing LearningMemoryEvent.
// They are created exclusively by LearningMemoryEventFactory.
// They carry no logic — only data about what happened.
// =====================================================================

/**
 * Fired when the [com.learnwitme.core.domain.model.LearningMemory] aggregate
 * has been updated by the Update Engine after a completed analysis cycle.
 *
 * This is the primary event in the Learning Memory lifecycle. It carries:
 * - A before/after snapshot pair for full state comparison
 * - The ordered list of [appliedUpdates] that were applied in this cycle
 * - The count of meaningful changes for quick filtering
 *
 * Consumers (application layer):
 * - Audit log system → records the full transition
 * - Recommendation Engine → checks for new strengths/weaknesses
 * - UI layer → decides whether to surface any learning insight
 *
 * @param eventId              Unique ID for this event instance.
 * @param memoryId             ID of the affected LearningMemory aggregate.
 * @param userId               ID of the learner.
 * @param occurredAtMs         Epoch ms when the update was applied.
 * @param previousSnapshot     Lightweight snapshot of the memory state BEFORE the update.
 * @param currentSnapshot      Lightweight snapshot of the memory state AFTER the update.
 * @param appliedUpdates       The ordered audit trail of updates applied in this cycle.
 * @param meaningfulUpdateCount Number of updates that were not NO_CHANGE.
 */
data class LearningMemoryUpdatedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val previousSnapshot: MemoryStateSnapshot,
    val currentSnapshot: MemoryStateSnapshot,
    val appliedUpdates: List<LearningMemoryUpdate>,
    val meaningfulUpdateCount: Int
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.MEMORY_UPDATED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
        require(meaningfulUpdateCount >= 0) {
            "meaningfulUpdateCount cannot be negative (got $meaningfulUpdateCount)"
        }
    }

    /** True if any strength was added or confirmed in this cycle. */
    val hasStrengthChange: Boolean
        get() = appliedUpdates.any { it.affectsStrength }

    /** True if any weakness was added, updated, worsened, or resolved in this cycle. */
    val hasWeaknessChange: Boolean
        get() = appliedUpdates.any { it.affectsWeakness }

    /** True if this update had at least one meaningful change. */
    val hasAnyChange: Boolean get() = meaningfulUpdateCount > 0
}

/**
 * Fired when a [LearningMemorySnapshot] has been created.
 *
 * A snapshot represents the full, frozen state of a LearningMemory at a
 * point in time. This event signals that the snapshot exists and is ready
 * for persistence by the infrastructure layer.
 *
 * Consumers:
 * - Repository layer → persists the snapshot to storage
 * - History view → knows a new checkpoint is available
 *
 * @param eventId      Unique ID for this event instance.
 * @param memoryId     ID of the LearningMemory aggregate.
 * @param userId       ID of the learner.
 * @param occurredAtMs Epoch ms when the snapshot was captured.
 * @param snapshotId   The ID of the created snapshot (matches LearningMemorySnapshot.snapshotId).
 * @param snapshotReason Human-readable reason for the snapshot.
 * @param snapshot     The full created snapshot.
 */
data class LearningMemorySnapshotCreatedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val snapshotId: String,
    val snapshotReason: String,
    val snapshot: LearningMemorySnapshot
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.SNAPSHOT_CREATED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
        require(snapshotId.isNotBlank()) { "Snapshot ID cannot be blank" }
        require(snapshotReason.isNotBlank()) { "Snapshot reason cannot be blank" }
    }
}

/**
 * Fired when a learner's cognitive learning style is determined for the first time
 * (transitions from [LearningStyleType.UNDETERMINED] to a concrete style).
 *
 * This is a significant one-time lifecycle milestone. The Recommendation Engine
 * can use this to unlock personalized content sequencing.
 *
 * @param eventId           Unique ID for this event instance.
 * @param memoryId          ID of the LearningMemory aggregate.
 * @param userId            ID of the learner.
 * @param occurredAtMs      Epoch ms when the determination occurred.
 * @param determinedStyle   The newly determined primary learning style.
 * @param confidenceScore   The confidence level of the determination [0.0, 1.0].
 * @param observationCount  Number of sessions that contributed to this determination.
 */
data class LearningMemoryPreferenceDeterminedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val determinedStyle: LearningStyleType,
    val confidenceScore: Double,
    val observationCount: Int
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.PREFERENCE_DETERMINED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
        require(determinedStyle != LearningStyleType.UNDETERMINED) {
            "PreferenceDetermined event requires a concrete style, not UNDETERMINED"
        }
        require(confidenceScore in 0.0..1.0) {
            "Confidence score must be in [0.0, 1.0] (got $confidenceScore)"
        }
        require(observationCount > 0) {
            "Observation count must be positive (got $observationCount)"
        }
    }
}

/**
 * Fired when a topic strength is detected for the first time.
 *
 * "Detected" means the topic was not previously tracked as a Strength
 * and has just crossed the mastery threshold for the first time.
 * Re-confirmations of existing strengths do NOT fire this event.
 *
 * @param eventId          Unique ID for this event instance.
 * @param memoryId         ID of the LearningMemory aggregate.
 * @param userId           ID of the learner.
 * @param occurredAtMs     Epoch ms when the strength was detected.
 * @param strength         The newly detected [LearningStrength].
 */
data class LearningMemoryStrengthDetectedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val strength: LearningStrength
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.STRENGTH_DETECTED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
    }

    /** The topic code for quick filtering downstream. */
    val topicCode: String get() = strength.topicCode

    /** The topic name for display purposes. */
    val topicName: String get() = strength.topicName
}

/**
 * Fired when a topic weakness reaches [WeaknessSeverity.SEVERE] level.
 *
 * Severity SEVERE means the learner has persistent, consecutive failures
 * on this topic. This event signals the Recommendation Engine to urgently
 * prioritize remediation content.
 *
 * @param eventId             Unique ID for this event instance.
 * @param memoryId            ID of the LearningMemory aggregate.
 * @param userId              ID of the learner.
 * @param occurredAtMs        Epoch ms when the severe weakness was detected.
 * @param weakness            The [LearningWeakness] at SEVERE level.
 * @param isNewWeakness       True if this topic was not previously tracked as a weakness.
 *                            False if it was already tracked but has now escalated to SEVERE.
 */
data class LearningMemorySevereWeaknessDetectedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val weakness: LearningWeakness,
    val isNewWeakness: Boolean
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.SEVERE_WEAKNESS_DETECTED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
        require(weakness.severity == WeaknessSeverity.SEVERE) {
            "SevereWeaknessDetected event requires a SEVERE weakness (got ${weakness.severity})"
        }
    }

    /** The topic code for quick filtering downstream. */
    val topicCode: String get() = weakness.topicCode
}

/**
 * Fired when a topic that was previously tracked as a Weakness has been resolved.
 *
 * "Resolved" means the learner's retention and mastery have recovered above
 * the weakness threshold for this topic.
 *
 * @param eventId       Unique ID for this event instance.
 * @param memoryId      ID of the LearningMemory aggregate.
 * @param userId        ID of the learner.
 * @param occurredAtMs  Epoch ms when the weakness was resolved.
 * @param topicCode     The topic code that was resolved.
 * @param topicName     The topic name for display purposes.
 * @param resolvedAfterMs Duration (ms) the topic was tracked as a weakness before resolution.
 *                        Computed as: resolvedEpochMs - weakness.detectedEpochMs.
 */
data class LearningMemoryWeaknessResolvedEvent(
    override val eventId: String,
    override val memoryId: LearningMemoryId,
    override val userId: UserId,
    override val occurredAtMs: Long,
    val topicCode: String,
    val topicName: String,
    val resolvedAfterMs: Long
) : LearningMemoryEvent {

    override val type: LearningMemoryEventType = LearningMemoryEventType.WEAKNESS_RESOLVED

    init {
        require(eventId.isNotBlank()) { "Event ID cannot be blank" }
        require(occurredAtMs > 0) { "occurredAtMs must be positive (got $occurredAtMs)" }
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
        require(topicName.isNotBlank()) { "Topic name cannot be blank" }
        require(resolvedAfterMs >= 0) {
            "resolvedAfterMs cannot be negative (got $resolvedAfterMs)"
        }
    }

    /** Duration in hours the weakness was tracked before resolution. */
    val resolvedAfterHours: Double get() = resolvedAfterMs / 3_600_000.0
}

// ─────────────────────────────────────────────────────────────────────
// MemoryStateSnapshot
// ─────────────────────────────────────────────────────────────────────

/**
 * A lightweight, immutable projection of a [com.learnwitme.core.domain.model.LearningMemory]
 * state at a specific point in time.
 *
 * Used as the before/after payload in [LearningMemoryUpdatedEvent].
 * Lighter than [LearningMemorySnapshot] — carries only summary-level data
 * relevant for event consumers, without the full record list.
 *
 * @param strengthCount        Number of topic strengths at this point.
 * @param weaknessCount        Number of topic weaknesses at this point.
 * @param behaviorCount        Number of detected behaviors at this point.
 * @param primaryStyle         Primary learning style at this point.
 * @param overallRetention     Average retention score at this point [0.0, 1.0].
 * @param capturedAtMs         Epoch ms when this projection was captured.
 */
data class MemoryStateSnapshot(
    val strengthCount: Int,
    val weaknessCount: Int,
    val behaviorCount: Int,
    val primaryStyle: LearningStyleType,
    val overallRetention: Double,
    val capturedAtMs: Long
) : ValueObject {

    init {
        require(strengthCount >= 0) { "strengthCount cannot be negative (got $strengthCount)" }
        require(weaknessCount >= 0) { "weaknessCount cannot be negative (got $weaknessCount)" }
        require(behaviorCount >= 0) { "behaviorCount cannot be negative (got $behaviorCount)" }
        require(overallRetention in 0.0..1.0) {
            "overallRetention must be in [0.0, 1.0] (got $overallRetention)"
        }
        require(capturedAtMs > 0) { "capturedAtMs must be positive (got $capturedAtMs)" }
    }

    /** True if this state has at least one tracked strength. */
    val hasStrengths: Boolean get() = strengthCount > 0

    /** True if this state has at least one tracked weakness. */
    val hasWeaknesses: Boolean get() = weaknessCount > 0
}
