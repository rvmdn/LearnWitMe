package com.learnwitme.feature.knowledgecard

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningInteractionService
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.RoadmapRepository
import com.learnwitme.core.domain.service.Clock
import com.learnwitme.core.domain.usecase.CompleteLearningSessionWorkflow
import com.learnwitme.core.domain.usecase.FinishReviewWorkflow
import com.learnwitme.core.domain.usecase.LoadKnowledgeCardWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeCardViewModel @Inject constructor(
    private val loadCardWorkflow: LoadKnowledgeCardWorkflow,
    private val finishReviewWorkflow: FinishReviewWorkflow,
    private val completeLearningSessionWorkflow: CompleteLearningSessionWorkflow,
    private val roadmapRepository: RoadmapRepository,
    private val clock: Clock,
    private val aiLearningInteractionService: AILearningInteractionService
) : BaseViewModel<KnowledgeCardState, KnowledgeCardEvent, KnowledgeCardEffect>() {

    private val currentUserId = UserIdentity.Guest.DEFAULT_GUEST_ID
    private var sessionStartTime: Long = 0L

    override fun createInitialState(): KnowledgeCardState = KnowledgeCardState.Loading

    override fun handleEvent(event: KnowledgeCardEvent) {
        when (event) {
            is KnowledgeCardEvent.OnLoadCard -> {
                loadCard(event.cardId)
            }
            is KnowledgeCardEvent.OnSubmitReview -> {
                submitReview(event.cardId, event.rating)
            }
            is KnowledgeCardEvent.OnRequestAiExplanation -> {
                requestAiExplanation(event.cardId)
            }
            is KnowledgeCardEvent.OnSubmitAiFeedback -> {
                submitAiFeedback(event.interactionId, event.rating, event.comment)
            }
            is KnowledgeCardEvent.OnDismissAiExplanation -> {
                dismissAiExplanation()
            }
        }
    }

    private fun loadCard(cardId: String) {
        viewModelScope.launch {
            setState { KnowledgeCardState.Loading }
            sessionStartTime = clock.currentTimeMillis()
            try {
                val card = loadCardWorkflow.execute(KnowledgeCardId(cardId))
                if (card != null) {
                    setState { KnowledgeCardState.Success(card = card, aiState = AiAssistanceState.Idle) }
                } else {
                    setState { KnowledgeCardState.Error("Card not found: $cardId") }
                }
            } catch (e: Exception) {
                setState { KnowledgeCardState.Error(e.message ?: "Failed to load card") }
            }
        }
    }

    private fun submitReview(cardId: String, rating: Double) {
        viewModelScope.launch {
            try {
                val effectiveStartTime = if (sessionStartTime > 0L) sessionStartTime else clock.currentTimeMillis() - 1000L
                val now = clock.currentTimeMillis()
                val quality = ReviewQuality(
                    accuracy = if (rating >= 0.6) 1.0 else 0.0,
                    hintUsed = false,
                    responseTimeMs = (now - effectiveStartTime).coerceAtLeast(1000L),
                    confidenceRating = rating
                )
                finishReviewWorkflow.execute(
                    userId = currentUserId,
                    cardId = KnowledgeCardId(cardId),
                    quality = quality,
                    cardToTopicMapper = { "default" }
                )

                val activeRoadmap = roadmapRepository.getActiveRoadmapByUserId(currentUserId)
                val unit = activeRoadmap?.units?.find { it.cardId == KnowledgeCardId(cardId) }
                if (activeRoadmap != null && unit != null) {
                    val session = LearningSession(
                        id = LearningSessionId("sess_${cardId}_$now"),
                        startTime = effectiveStartTime,
                        endTime = now,
                        completedUnitIds = listOf(unit.id)
                    )
                    completeLearningSessionWorkflow.execute(
                        userId = currentUserId,
                        roadmapId = activeRoadmap.id,
                        session = session
                    )
                }

                setEffect { KnowledgeCardEffect.ShowToast("Review recorded and progress updated!") }
                setEffect { KnowledgeCardEffect.NavigateBack }
            } catch (e: Exception) {
                setEffect { KnowledgeCardEffect.ShowToast(e.message ?: "Failed to submit review") }
            }
        }
    }

    private fun requestAiExplanation(cardId: String) {
        val current = currentState
        if (current is KnowledgeCardState.Success) {
            viewModelScope.launch {
                setState { current.copy(aiState = AiAssistanceState.Loading) }
                try {
                    val interactionId = "ai_int_${cardId}_${clock.currentTimeMillis()}"
                    val startTime = clock.currentTimeMillis()
                    val result = aiLearningInteractionService.executeExplanation(
                        interactionId = interactionId,
                        userId = currentUserId,
                        question = "Explain core concepts, practical examples, and common pitfalls of ${current.card.title}",
                        selectedAnswer = current.card.summary,
                        expectedAnswer = current.card.whyItMatters,
                        startedAt = startTime,
                        completedAt = clock.currentTimeMillis(),
                        cardId = current.card.id,
                        policy = PersonalizedPromptPolicy.EXPLANATION_ONLY
                    )
                    val stateNow = currentState
                    if (stateNow is KnowledgeCardState.Success) {
                        setState {
                            stateNow.copy(
                                aiState = AiAssistanceState.Success(
                                    interactionId = interactionId,
                                    explanation = result.generatedResult.content
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    val stateNow = currentState
                    if (stateNow is KnowledgeCardState.Success) {
                        setState {
                            stateNow.copy(
                                aiState = AiAssistanceState.Error(
                                    message = e.message ?: "Failed to generate AI explanation"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun submitAiFeedback(interactionId: String, rating: Double, comment: String?) {
        val current = currentState
        if (current is KnowledgeCardState.Success && current.aiState is AiAssistanceState.Success) {
            viewModelScope.launch {
                try {
                    val feedback = AILearningFeedback(
                        interactionId = interactionId,
                        usefulnessRating = if (rating >= 0.6) 5 else 2,
                        confidenceSignal = rating.coerceIn(0.0, 1.0),
                        comment = comment,
                        createdAt = clock.currentTimeMillis()
                    )
                    aiLearningInteractionService.executeExplanation(
                        interactionId = interactionId,
                        userId = currentUserId,
                        question = "Explain core concepts, practical examples, and common pitfalls of ${current.card.title}",
                        selectedAnswer = current.card.summary,
                        expectedAnswer = current.card.whyItMatters,
                        startedAt = clock.currentTimeMillis() - 1000L,
                        completedAt = clock.currentTimeMillis(),
                        cardId = current.card.id,
                        policy = PersonalizedPromptPolicy.EXPLANATION_ONLY,
                        feedback = feedback
                    )
                    val stateNow = currentState
                    if (stateNow is KnowledgeCardState.Success && stateNow.aiState is AiAssistanceState.Success) {
                        setState {
                            stateNow.copy(
                                aiState = (stateNow.aiState as AiAssistanceState.Success).copy(feedbackSubmitted = true)
                            )
                        }
                    }
                    setEffect { KnowledgeCardEffect.ShowToast("Thank you for your feedback!") }
                } catch (e: Exception) {
                    setEffect { KnowledgeCardEffect.ShowToast(e.message ?: "Failed to submit feedback") }
                }
            }
        }
    }

    private fun dismissAiExplanation() {
        val current = currentState
        if (current is KnowledgeCardState.Success) {
            setState { current.copy(aiState = AiAssistanceState.Idle) }
        }
    }
}
