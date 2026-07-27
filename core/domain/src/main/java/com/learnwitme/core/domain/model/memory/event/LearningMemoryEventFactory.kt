package com.learnwitme.core.domain.model.memory.event

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.LearningMemorySnapshot
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryEngineResult
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdateType

// =====================================================================
// Learning Memory — LearningMemoryEventFactory
// Sprint 9 · Stage 4 · Learning Memory Events & History
//
// Responsibility: Creates Learning Memory domain events deterministically
// from available domain inputs.
//
// The factory:
//   1. Derives events from LearningMemoryEngineResult (post-update state)
//   2. Produces LearningMemoryUpdatedEvent for every successful update cycle
//   3. Derives milestone events (PreferenceDetermined, StrengthDetected, etc.)
//      by inspecting the applied updates for significant transitions
//   4. Creates LearningMemorySnapshotCreatedEvent when a snapshot is taken
//
// Event ID generation:
//   The factory accepts an IdGenerator function — a pure (String) -> String
//   mapping from a seed to a unique ID. This keeps the factory
//   infrastructure-free: no UUID library is imported here.
//   The caller (application layer) provides a UUID-based generator.
//
// Determinism guarantee:
//   Same EngineResult + same idGenerator + same nowEpochMs → same events.
//
// Ordering contract:
//   fromEngineResult() always produces events in this order:
//     1. LearningMemoryUpdatedEvent (always present)
//     2. LearningMemoryPreferenceDeterminedEvent (if applicable)
//     3. LearningMemoryStrengthDetectedEvent (0..N, one per new strength)
//     4. LearningMemorySevereWeaknessDetectedEvent (0..N, one per severe weakness)
//     5. LearningMemoryWeaknessResolvedEvent (0..N, one per resolved weakness)
// =====================================================================

/**
 * Responsibility: Produces all Learning Memory domain events deterministically
 * from a [LearningMemoryEngineResult] or an explicit [LearningMemorySnapshot].
 *
 * Events are created consistently — no caller needs to construct event data
 * directly. All event field assignments follow the same derivation logic.
 *
 * Fully stateless — no instance state.
 *
 * @param idGenerator A pure function that generates a unique event ID string
 *                    from a seed string. Typically a UUID generator injected
 *                    by the application layer (e.g., `{ UUID.randomUUID().toString() }`).
 */
class LearningMemoryEventFactory(
    private val idGenerator: (seed: String) -> String
) {

    // ─────────────────────────────────────────────────────────────────
    // Primary factory: produce all events from an engine result
    // ─────────────────────────────────────────────────────────────────

    /**
     * Derives all relevant domain events from a completed [LearningMemoryEngineResult].
     *
     * Always produces at least one event: [LearningMemoryUpdatedEvent].
     * Additionally produces milestone events for significant state transitions.
     *
     * @param engineResult  The result from [com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdateEngine].
     * @param previousMemory The [LearningMemory] state BEFORE the engine was applied.
     *                       Used to derive the [MemoryStateSnapshot] for the before-state.
     * @return An ordered, non-empty list of domain events. Order is defined in the file header.
     */
    fun fromEngineResult(
        engineResult: LearningMemoryEngineResult,
        previousMemory: LearningMemory
    ): List<LearningMemoryEvent> {

        val nowMs = engineResult.computedEpochMs
        val memoryId = engineResult.updatedMemory.id
        val userId = engineResult.updatedMemory.userId
        val events = mutableListOf<LearningMemoryEvent>()

        // ── 1. Always: LearningMemoryUpdatedEvent ─────────────────────
        val previousSnapshot = previousMemory.toStateSnapshot(nowMs)
        val currentSnapshot = engineResult.updatedMemory.toStateSnapshot(nowMs)
        events.add(
            LearningMemoryUpdatedEvent(
                eventId = idGenerator("updated_${memoryId.value}_$nowMs"),
                memoryId = memoryId,
                userId = userId,
                occurredAtMs = nowMs,
                previousSnapshot = previousSnapshot,
                currentSnapshot = currentSnapshot,
                appliedUpdates = engineResult.appliedUpdates.toList(),
                meaningfulUpdateCount = engineResult.meaningfulUpdateCount
            )
        )

        // ── 2. Milestone: PreferenceDetermined ────────────────────────
        // Fires when a PREFERENCE_DETERMINED update was applied (style was unknown before)
        val preferenceWasDetermined = engineResult.appliedUpdates.any {
            it.type == LearningMemoryUpdateType.PREFERENCE_DETERMINED
        }
        if (preferenceWasDetermined) {
            val pref = engineResult.updatedMemory.preference
            if (pref.isDetermined && pref.primaryStyle != LearningStyleType.UNDETERMINED) {
                events.add(
                    LearningMemoryPreferenceDeterminedEvent(
                        eventId = idGenerator("pref_determined_${memoryId.value}_$nowMs"),
                        memoryId = memoryId,
                        userId = userId,
                        occurredAtMs = nowMs,
                        determinedStyle = pref.primaryStyle,
                        confidenceScore = pref.confidenceScore,
                        observationCount = pref.observationCount
                    )
                )
            }
        }

        // ── 3. Milestone: StrengthDetected (one per NEW strength) ──────
        // Fires only for STRENGTH_ADDED — not for STRENGTH_CONFIRMED
        val addedStrengthUpdates = engineResult.appliedUpdates.filter {
            it.type == LearningMemoryUpdateType.STRENGTH_ADDED && it.strength != null
        }
        for (update in addedStrengthUpdates) {
            val strength = update.strength ?: continue
            events.add(
                LearningMemoryStrengthDetectedEvent(
                    eventId = idGenerator("strength_detected_${strength.topicCode}_$nowMs"),
                    memoryId = memoryId,
                    userId = userId,
                    occurredAtMs = nowMs,
                    strength = strength
                )
            )
        }

        // ── 4. Milestone: SevereWeaknessDetected ──────────────────────
        // Fires for any weakness update that is SEVERE (added or worsened)
        val severeWeaknessUpdates = engineResult.appliedUpdates.filter { update ->
            (update.type == LearningMemoryUpdateType.WEAKNESS_ADDED ||
                    update.type == LearningMemoryUpdateType.WEAKNESS_WORSENED) &&
                    update.weakness?.severity == WeaknessSeverity.SEVERE
        }
        for (update in severeWeaknessUpdates) {
            val weakness = update.weakness ?: continue
            val isNew = update.type == LearningMemoryUpdateType.WEAKNESS_ADDED
            events.add(
                LearningMemorySevereWeaknessDetectedEvent(
                    eventId = idGenerator("severe_weakness_${weakness.topicCode}_$nowMs"),
                    memoryId = memoryId,
                    userId = userId,
                    occurredAtMs = nowMs,
                    weakness = weakness,
                    isNewWeakness = isNew
                )
            )
        }

        // ── 5. Milestone: WeaknessResolved ────────────────────────────
        // One event per resolved weakness topic code
        val resolvedWeaknessUpdates = engineResult.appliedUpdates.filter {
            it.type == LearningMemoryUpdateType.WEAKNESS_RESOLVED &&
                    it.topicCode != null
        }
        for (update in resolvedWeaknessUpdates) {
            val topicCode = update.topicCode ?: continue

            // Look up this weakness in the previous memory state for name + detectedEpochMs
            val previousWeakness = previousMemory.weaknesses.find { it.topicCode == topicCode }
            val topicName = previousWeakness?.topicName ?: topicCode
            val resolvedAfterMs = if (previousWeakness != null) {
                (nowMs - previousWeakness.detectedEpochMs).coerceAtLeast(0L)
            } else 0L

            events.add(
                LearningMemoryWeaknessResolvedEvent(
                    eventId = idGenerator("weakness_resolved_${topicCode}_$nowMs"),
                    memoryId = memoryId,
                    userId = userId,
                    occurredAtMs = nowMs,
                    topicCode = topicCode,
                    topicName = topicName,
                    resolvedAfterMs = resolvedAfterMs
                )
            )
        }

        return events.toList()
    }

    // ─────────────────────────────────────────────────────────────────
    // Snapshot event factory
    // ─────────────────────────────────────────────────────────────────

    /**
     * Creates a [LearningMemorySnapshotCreatedEvent] for a snapshot that has just been taken.
     *
     * @param snapshot   The snapshot that was created.
     * @param memoryId   ID of the affected LearningMemory aggregate.
     * @param userId     ID of the learner.
     * @param nowEpochMs Epoch ms when the snapshot was created.
     * @return The corresponding snapshot event.
     */
    fun fromSnapshot(
        snapshot: LearningMemorySnapshot,
        memoryId: LearningMemoryId,
        userId: UserId,
        nowEpochMs: Long
    ): LearningMemorySnapshotCreatedEvent {
        return LearningMemorySnapshotCreatedEvent(
            eventId = idGenerator("snapshot_created_${memoryId.value}_$nowEpochMs"),
            memoryId = memoryId,
            userId = userId,
            occurredAtMs = nowEpochMs,
            snapshotId = snapshot.snapshotId,
            snapshotReason = snapshot.snapshotReason,
            snapshot = snapshot
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Individual event factories (for application-layer use)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Creates a [LearningMemoryUpdatedEvent] directly from a before/after memory pair.
     *
     * Used when the caller manages the engine result externally and needs to
     * create an event without going through [fromEngineResult].
     */
    fun updatedEvent(
        previousMemory: LearningMemory,
        updatedMemory: LearningMemory,
        appliedUpdates: List<com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdate>,
        meaningfulUpdateCount: Int,
        nowEpochMs: Long
    ): LearningMemoryUpdatedEvent {
        val memoryId = updatedMemory.id
        return LearningMemoryUpdatedEvent(
            eventId = idGenerator("updated_${memoryId.value}_$nowEpochMs"),
            memoryId = memoryId,
            userId = updatedMemory.userId,
            occurredAtMs = nowEpochMs,
            previousSnapshot = previousMemory.toStateSnapshot(nowEpochMs),
            currentSnapshot = updatedMemory.toStateSnapshot(nowEpochMs),
            appliedUpdates = appliedUpdates.toList(),
            meaningfulUpdateCount = meaningfulUpdateCount
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// LearningMemory extension — toStateSnapshot()
// ─────────────────────────────────────────────────────────────────────

/**
 * Produces a lightweight [MemoryStateSnapshot] from this [LearningMemory].
 * Used as the before/after state in [LearningMemoryUpdatedEvent].
 */
internal fun LearningMemory.toStateSnapshot(capturedAtMs: Long): MemoryStateSnapshot =
    MemoryStateSnapshot(
        strengthCount = strengths.size,
        weaknessCount = weaknesses.size,
        behaviorCount = behaviors.size,
        primaryStyle = preference.primaryStyle,
        overallRetention = computeOverallRetention(),
        capturedAtMs = capturedAtMs
    )
