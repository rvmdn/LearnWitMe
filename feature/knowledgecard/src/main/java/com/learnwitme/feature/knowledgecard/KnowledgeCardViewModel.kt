package com.learnwitme.feature.knowledgecard

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.FinishReviewWorkflow
import com.learnwitme.core.domain.usecase.LoadKnowledgeCardWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeCardViewModel @Inject constructor(
    private val loadCardWorkflow: LoadKnowledgeCardWorkflow,
    private val finishReviewWorkflow: FinishReviewWorkflow
) : BaseViewModel<KnowledgeCardState, KnowledgeCardEvent, KnowledgeCardEffect>() {

    private val currentUserId = UserId("current_user")

    override fun createInitialState(): KnowledgeCardState = KnowledgeCardState.Loading

    override fun handleEvent(event: KnowledgeCardEvent) {
        when (event) {
            is KnowledgeCardEvent.OnLoadCard -> {
                loadCard(event.cardId)
            }
            is KnowledgeCardEvent.OnSubmitReview -> {
                submitReview(event.cardId, event.rating)
            }
        }
    }

    private fun loadCard(cardId: String) {
        viewModelScope.launch {
            setState { KnowledgeCardState.Loading }
            try {
                val card = loadCardWorkflow.execute(KnowledgeCardId(cardId))
                if (card != null) {
                    setState { KnowledgeCardState.Success(card) }
                } else {
                    setState { KnowledgeCardState.Error("Card not found") }
                }
            } catch (e: Exception) {
                setState { KnowledgeCardState.Error(e.message ?: "Failed to load card") }
            }
        }
    }

    private fun submitReview(cardId: String, rating: Double) {
        viewModelScope.launch {
            try {
                val quality = ReviewQuality(
                    accuracy = if (rating >= 0.6) 1.0 else 0.0,
                    hintUsed = false,
                    responseTimeMs = 1000L,
                    confidenceRating = rating
                )
                finishReviewWorkflow.execute(
                    userId = currentUserId,
                    cardId = KnowledgeCardId(cardId),
                    quality = quality,
                    cardToTopicMapper = { "default" }
                )
                setEffect { KnowledgeCardEffect.ShowToast("Review recorded!") }
                setEffect { KnowledgeCardEffect.NavigateBack }
            } catch (e: Exception) {
                setEffect { KnowledgeCardEffect.ShowToast(e.message ?: "Failed to submit review") }
            }
        }
    }
}
