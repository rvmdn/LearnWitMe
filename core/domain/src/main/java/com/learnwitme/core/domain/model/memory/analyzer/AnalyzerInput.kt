package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryReview
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.ValueObject

// =====================================================================
// Learning Memory — AnalyzerInput
// Sprint 9 · Stage 2 · Learning Memory Analyzer
//
// Responsibility: Defines the shared input contract for all
// Learning Memory analyzers. All analyzers consume the same
// immutable input — MemoryAnalysisInput — ensuring consistent
// data feeding and no hidden coupling between analyzers.
//
// Architecture:
//   LearningSession data + MemoryRecords
//   ↓
//   MemoryAnalysisInput (assembled by caller)
//   ↓
//   Analyzers (stateless, pure functions)
//   ↓
//   LearningMemoryAnalysisResult (aggregate output)
// =====================================================================

/**
 * Represents observable data from a single completed learning session,
 * extracted and passed to analyzers. This is not a persistence entity —
 * it is a transient read-only projection for analysis.
 *
 * @param sessionId           Identifier for this session (opaque string).
 * @param startEpochMs        Session start time (epoch ms).
 * @param endEpochMs          Session end time (epoch ms). Must be >= [startEpochMs].
 * @param completed           Whether the session was completed (vs abandoned/cancelled).
 * @param cardInteractions    Ordered list of interactions that occurred during the session.
 */
data class SessionObservation(
    val sessionId: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val completed: Boolean,
    val cardInteractions: List<CardInteraction>
) : ValueObject {

    init {
        require(sessionId.isNotBlank()) { "Session ID cannot be blank" }
        require(startEpochMs > 0) { "Session start time must be a positive epoch value (got $startEpochMs)" }
        require(endEpochMs >= startEpochMs) { "Session end time cannot be before start time" }
    }

    /** Session duration in milliseconds. */
    val durationMs: Long get() = endEpochMs - startEpochMs

    /** Session duration in minutes. */
    val durationMinutes: Double get() = durationMs / 60_000.0

    /** Total number of card interactions in this session. */
    val totalInteractions: Int get() = cardInteractions.size

    /** Count of quiz interactions that were skipped. */
    val skippedQuizCount: Int get() = cardInteractions.count { it.wasSkipped && it.isQuiz }

    /** Count of interactions where a hint was used. */
    val hintUsedCount: Int get() = cardInteractions.count { it.hintUsed }

    /** Count of interactions with accuracy < 0.6 (failed). */
    val failedInteractionCount: Int get() = cardInteractions.count { it.accuracy < 0.6 }
}

/**
 * Represents a single observable interaction with a KnowledgeCard during a session.
 *
 * @param cardId              The card that was interacted with.
 * @param topicCode           The topic this card belongs to (e.g., "tp_networking"). Must start with "tp_".
 * @param topicName           Human-readable topic label.
 * @param isQuiz              True if the interaction was a quiz/recall attempt; false if reading/review.
 * @param wasSkipped          True if the learner skipped this card without engaging.
 * @param hintUsed            True if the learner used a hint during this interaction.
 * @param accuracy            Accuracy score for this interaction [0.0, 1.0]. 0.0 if skipped.
 * @param responseTimeMs      Time taken to respond in milliseconds. 0 if skipped.
 * @param confidenceRating    Self-reported confidence after the interaction [0.0, 1.0].
 * @param readCount           How many times the card was read in this session (for reading cards).
 * @param interactionEpochMs  Epoch timestamp of this interaction.
 */
data class CardInteraction(
    val cardId: KnowledgeCardId,
    val topicCode: String,
    val topicName: String,
    val isQuiz: Boolean,
    val wasSkipped: Boolean,
    val hintUsed: Boolean,
    val accuracy: Double,
    val responseTimeMs: Long,
    val confidenceRating: Double,
    val readCount: Int,
    val interactionEpochMs: Long
) : ValueObject {

    init {
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
        require(topicCode.startsWith("tp_")) {
            "Topic code must start with 'tp_' (got '$topicCode')"
        }
        require(topicName.isNotBlank()) { "Topic name cannot be blank" }
        require(accuracy in 0.0..1.0) { "Accuracy must be in [0.0, 1.0] (got $accuracy)" }
        require(responseTimeMs >= 0) { "Response time cannot be negative (got $responseTimeMs)" }
        require(confidenceRating in 0.0..1.0) {
            "Confidence rating must be in [0.0, 1.0] (got $confidenceRating)"
        }
        require(readCount >= 0) { "Read count cannot be negative (got $readCount)" }
        require(interactionEpochMs > 0) { "Interaction timestamp must be positive (got $interactionEpochMs)" }
    }
}

/**
 * The shared, immutable input contract consumed by all Learning Memory analyzers.
 *
 * Assembled once from available session and memory data; passed to each
 * analyzer independently. No analyzer mutates this input or calls repositories.
 *
 * @param memoryRecords          Current per-card memory records for the learner.
 * @param recentSessions         Historical session observations, ordered chronologically (oldest first).
 * @param cardToTopicMapper      Pure function mapping a card ID to its topic code. Must return "tp_"-prefixed codes.
 * @param cardToTopicNameMapper  Pure function mapping a card ID to its topic name.
 * @param nowEpochMs             Current epoch timestamp for use in analysis computations.
 */
data class MemoryAnalysisInput(
    val memoryRecords: List<MemoryRecord>,
    val recentSessions: List<SessionObservation>,
    val cardToTopicMapper: (KnowledgeCardId) -> String,
    val cardToTopicNameMapper: (KnowledgeCardId) -> String,
    val nowEpochMs: Long
) {
    init {
        require(nowEpochMs > 0) { "nowEpochMs must be a positive epoch value (got $nowEpochMs)" }
    }

    /** Total number of cards being tracked. */
    val totalCardsTracked: Int get() = memoryRecords.size

    /** Total number of sessions available for analysis. */
    val totalSessionCount: Int get() = recentSessions.size

    /** Convenience: returns all records in a given memory state. */
    fun recordsByState(state: MemoryState): List<MemoryRecord> =
        memoryRecords.filter { it.state == state }

    /** Convenience: returns records grouped by topic code. */
    fun recordsByTopic(): Map<String, List<MemoryRecord>> =
        memoryRecords.groupBy { cardToTopicMapper(it.cardId) }

    /** Convenience: all card interactions across all sessions, flattened. */
    val allInteractions: List<CardInteraction> get() =
        recentSessions.flatMap { it.cardInteractions }

    /** Convenience: all quiz interactions across all sessions. */
    val allQuizInteractions: List<CardInteraction> get() =
        allInteractions.filter { it.isQuiz }
}
