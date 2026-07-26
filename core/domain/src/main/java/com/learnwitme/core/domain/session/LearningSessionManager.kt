package com.learnwitme.core.domain.session

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.usecase.CompleteReviewUseCase
import com.learnwitme.core.domain.usecase.GenerateDailyRecommendationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Responsibility: Deterministic State Machine engine managing the full lifecycle of a Learning Session,
 * coordinating daily card generation, card presentation order, spaced-repetition review processing,
 * pause/resume flows, and final metrics generation.
 */
class LearningSessionManager(
    private val generateDailyRecommendationUseCase: GenerateDailyRecommendationUseCase,
    private val completeReviewUseCase: CompleteReviewUseCase
) {
    private val _state = MutableStateFlow<LearningSessionState>(LearningSessionState.Idle)
    val state: StateFlow<LearningSessionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LearningSessionEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<LearningSessionEvent> = _events.asSharedFlow()

    private val sessionCards = mutableListOf<KnowledgeCard>()
    private var currentIndex = 0
    private var cardsStudiedCount = 0
    private var correctAnswersCount = 0
    private var startTimeMs: Long = 0L

    suspend fun startSession(userId: UserId, availableCards: List<KnowledgeCard>, currentTimeMs: Long) {
        if (_state.value != LearningSessionState.Idle && _state.value !is LearningSessionState.Completed && _state.value != LearningSessionState.Cancelled) {
            return
        }
        _state.update { LearningSessionState.Loading }
        _events.emit(LearningSessionEvent.SessionStarted(userId.value))

        startTimeMs = currentTimeMs
        sessionCards.clear()

        val dailyFeed = generateDailyRecommendationUseCase.execute(userId, availableCards, currentTimeMs)
        val recommendedCardIds = dailyFeed.recommendations.map { it.cardId.value }.toSet()
        val sortedCards = availableCards.filter { recommendedCardIds.contains(it.id.value) }

        if (sortedCards.isEmpty() && availableCards.isNotEmpty()) {
            sessionCards.addAll(availableCards)
        } else {
            sessionCards.addAll(sortedCards)
        }

        currentIndex = 0
        cardsStudiedCount = 0
        correctAnswersCount = 0

        if (sessionCards.isNotEmpty()) {
            val firstCard = sessionCards.first()
            _state.update { LearningSessionState.Studying(firstCard, 0, sessionCards.size) }
            _events.emit(LearningSessionEvent.CardLoaded(firstCard.id.value))
        } else {
            finishSession(currentTimeMs)
        }
    }

    suspend fun submitAnswer(userId: UserId, qualityScore: Double, currentTimeMs: Long) {
        val currentState = _state.value as? LearningSessionState.Studying ?: return
        val currentCard = currentState.currentCard

        completeReviewUseCase.execute(userId, currentCard.id, qualityScore, currentTimeMs)

        cardsStudiedCount++
        if (qualityScore >= 0.6) {
            correctAnswersCount++
        }
        _events.emit(LearningSessionEvent.AnswerSubmitted(currentCard.id.value, qualityScore))

        advanceNextCard(currentTimeMs)
    }

    suspend fun skipCard(currentTimeMs: Long) {
        val currentState = _state.value as? LearningSessionState.Studying ?: return
        val currentCard = currentState.currentCard
        _events.emit(LearningSessionEvent.CardSkipped(currentCard.id.value))

        advanceNextCard(currentTimeMs)
    }

    suspend fun pauseSession() {
        if (_state.value is LearningSessionState.Studying) {
            _state.update { LearningSessionState.Paused }
            _events.emit(LearningSessionEvent.SessionPaused)
        }
    }

    suspend fun resumeSession() {
        if (_state.value == LearningSessionState.Paused && sessionCards.isNotEmpty() && currentIndex < sessionCards.size) {
            val currentCard = sessionCards[currentIndex]
            _state.update { LearningSessionState.Studying(currentCard, currentIndex, sessionCards.size) }
            _events.emit(LearningSessionEvent.SessionResumed)
        }
    }

    suspend fun finishSession(currentTimeMs: Long) {
        val elapsedSeconds = if (startTimeMs > 0) (currentTimeMs - startTimeMs) / 1000 else 0L
        val accuracy = if (cardsStudiedCount > 0) (correctAnswersCount.toDouble() / cardsStudiedCount.toDouble()) * 100.0 else 0.0
        val xp = (cardsStudiedCount * 10) + (correctAnswersCount * 5)

        val result = LearningSessionResult(
            cardsStudied = cardsStudiedCount,
            correctAnswers = correctAnswersCount,
            accuracyPercentage = accuracy,
            timeSpentSeconds = elapsedSeconds,
            xpEarned = xp,
            reviewsScheduled = cardsStudiedCount
        )

        _state.update { LearningSessionState.Completed(result) }
        _events.emit(LearningSessionEvent.SessionFinished(result))
    }

    suspend fun cancelSession() {
        _state.update { LearningSessionState.Cancelled }
        _events.emit(LearningSessionEvent.SessionCancelled)
    }

    private suspend fun advanceNextCard(currentTimeMs: Long) {
        currentIndex++
        if (currentIndex < sessionCards.size) {
            val nextCard = sessionCards[currentIndex]
            _state.update { LearningSessionState.Studying(nextCard, currentIndex, sessionCards.size) }
            _events.emit(LearningSessionEvent.CardLoaded(nextCard.id.value))
        } else {
            finishSession(currentTimeMs)
        }
    }
}
