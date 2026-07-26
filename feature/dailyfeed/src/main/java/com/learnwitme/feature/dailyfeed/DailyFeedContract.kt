package com.learnwitme.feature.dailyfeed

import com.learnwitme.core.domain.model.FeedDecision
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface DailyFeedState : UiState {
    object Loading : DailyFeedState
    data class Success(val feed: FeedDecision) : DailyFeedState
    data class Error(val message: String) : DailyFeedState
}

sealed interface DailyFeedEvent : UiEvent {
    object OnRefresh : DailyFeedEvent
    data class OnStartCard(val cardId: String) : DailyFeedEvent
}

sealed interface DailyFeedEffect : UiEffect {
    data class NavigateToCard(val cardId: String) : DailyFeedEffect
    data class ShowToast(val message: String) : DailyFeedEffect
}
