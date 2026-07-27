package com.learnwitme.core.domain.model.memory.event

import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId

// =====================================================================
// Learning Memory — LearningMemoryEvent (base)
// Sprint 9 · Stage 4 · Learning Memory Events & History
//
// Responsibility: Defines the base interface for all immutable Learning
// Memory domain events.
//
// Domain events describe WHAT happened — not HOW to persist it.
// They carry no side effects and make no external calls.
//
// Events are sealed within the `memory.event` package.
// They are produced by LearningMemoryEventFactory.
// They are consumed by the application layer for:
//   • Audit logging
//   • Triggering downstream domain reactions (e.g., snapshot creation)
//   • Future event sourcing
//
// Architecture contract:
//   - Events are immutable (all fields are val)
//   - Events carry a unique eventId (assigned by the factory)
//   - Events carry the userId and memoryId of the affected aggregate
//   - No event references a repository or service
//   - No event contains mutable state
// =====================================================================

/**
 * Classifies the type of a Learning Memory domain event.
 * Each variant maps directly to one concrete event subtype.
 */
enum class LearningMemoryEventType {

    /**
     * Fired when the LearningMemory aggregate has been updated by the
     * Update Engine as a result of a completed analysis cycle.
     * Carries the full before/after snapshots and the applied update list.
     */
    MEMORY_UPDATED,

    /**
     * Fired when a historical LearningMemorySnapshot has been created,
     * typically triggered after a MEMORY_UPDATED event to record state
     * at a meaningful checkpoint.
     */
    SNAPSHOT_CREATED,

    /**
     * Fired when a learner's cognitive learning style is first determined
     * (transitions from UNDETERMINED to a concrete style).
     * Distinct from MEMORY_UPDATED — this is a significant lifecycle milestone.
     */
    PREFERENCE_DETERMINED,

    /**
     * Fired when a topic strength is detected for the first time.
     * Significant enough to warrant its own event type for downstream
     * reactions (e.g., congratulatory feed item).
     */
    STRENGTH_DETECTED,

    /**
     * Fired when a severe topic weakness is detected — severity level SEVERE.
     * Triggers urgency signals to the Recommendation Engine.
     */
    SEVERE_WEAKNESS_DETECTED,

    /**
     * Fired when a topic weakness that was tracked has been fully resolved.
     * Signals recovery to the Recommendation Engine.
     */
    WEAKNESS_RESOLVED
}

/**
 * Base interface for all immutable Learning Memory domain events.
 *
 * Every event carries:
 * - [eventId]     — globally unique identifier for this event instance.
 * - [type]        — the category of this event.
 * - [memoryId]    — the ID of the LearningMemory aggregate that changed.
 * - [userId]      — the ID of the learner affected.
 * - [occurredAtMs] — epoch timestamp (ms) when this event occurred.
 *
 * Events are immutable records — they are never modified after creation.
 */
sealed interface LearningMemoryEvent {
    val eventId: String
    val type: LearningMemoryEventType
    val memoryId: LearningMemoryId
    val userId: UserId
    val occurredAtMs: Long
}
