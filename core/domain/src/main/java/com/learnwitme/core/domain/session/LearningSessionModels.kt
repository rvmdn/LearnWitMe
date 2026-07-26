package com.learnwitme.core.domain.session

import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Encapsulates final metrics summary of a completed learning session.
 */
data class LearningSessionResult(
    val cardsStudied: Int,
    val correctAnswers: Int,
    val accuracyPercentage: Double,
    val timeSpentSeconds: Long,
    val xpEarned: Int,
    val reviewsScheduled: Int
)

/**
 * Deterministic state hierarchy for a learning session state machine.
 */
sealed interface LearningSessionState {
    data object Idle : LearningSessionState
    data object Loading : LearningSessionState
    data class Studying(
        val currentCard: KnowledgeCard,
        val cardIndex: Int,
        val totalCards: Int
    ) : LearningSessionState
    data object Paused : LearningSessionState
    data class Completed(val result: LearningSessionResult) : LearningSessionState
    data object Cancelled : LearningSessionState
}

/**
 * Immutable business facts emitted during learning session lifecycle events.
 */
sealed interface LearningSessionEvent {
    data class SessionStarted(val userId: String) : LearningSessionEvent
    data class CardLoaded(val cardId: String) : LearningSessionEvent
    data class AnswerSubmitted(val cardId: String, val qualityScore: Double) : LearningSessionEvent
    data class CardSkipped(val cardId: String) : LearningSessionEvent
    data object SessionPaused : LearningSessionEvent
    data object SessionResumed : LearningSessionEvent
    data class SessionFinished(val result: LearningSessionResult) : LearningSessionEvent
    data object SessionCancelled : LearningSessionEvent
}
