package com.learnwitme.feature.dailyfeed

import com.learnwitme.core.domain.model.FeedDecision
import com.learnwitme.core.domain.model.memory.personalization.DailyInitializationState
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface DailyFeedState : UiState {
    object Loading : DailyFeedState

    data class Success(
        val feed: FeedDecision,
        val context: LearningPersonalizationContext,
        val initializationState: DailyInitializationState
    ) : DailyFeedState

    data class IncompletePersonalization(
        val reason: String
    ) : DailyFeedState

    data class ContentUnavailable(
        val reason: String
    ) : DailyFeedState

    data class Error(
        val message: String
    ) : DailyFeedState
}

sealed interface DailyFeedEvent : UiEvent {
    object OnRefresh : DailyFeedEvent
    data class OnStartCard(val cardId: String) : DailyFeedEvent
    object OnCompletePreferences : DailyFeedEvent
}

sealed interface DailyFeedEffect : UiEffect {
    data class NavigateToCard(val cardId: String) : DailyFeedEffect
    object NavigateToOnboarding : DailyFeedEffect
    data class ShowToast(val message: String) : DailyFeedEffect
}
