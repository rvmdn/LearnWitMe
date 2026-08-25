package com.learnwitme.feature.dailyfeed

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.UserIdentity
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.usecase.InitializePersonalizedDailyLearningWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyFeedViewModel @Inject constructor(
    private val initializePersonalizedDailyLearningWorkflow: InitializePersonalizedDailyLearningWorkflow
) : BaseViewModel<DailyFeedState, DailyFeedEvent, DailyFeedEffect>() {

    private val currentUserId = UserIdentity.Guest.DEFAULT_GUEST_ID

    init {
        loadFeed()
    }

    override fun createInitialState(): DailyFeedState = DailyFeedState.Loading

    override fun handleEvent(event: DailyFeedEvent) {
        when (event) {
            is DailyFeedEvent.OnRefresh -> {
                loadFeed()
            }
            is DailyFeedEvent.OnStartCard -> {
                setEffect { DailyFeedEffect.NavigateToCard(event.cardId) }
            }
            is DailyFeedEvent.OnCompletePreferences -> {
                setEffect { DailyFeedEffect.NavigateToOnboarding }
            }
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            setState { DailyFeedState.Loading }
            try {
                val result = initializePersonalizedDailyLearningWorkflow.execute(currentUserId)
                when (result) {
                    is DailyLearningInitializationResult.Success -> {
                        setState {
                            DailyFeedState.Success(
                                feed = result.feedDecision,
                                context = result.context,
                                initializationState = result.initializationState
                            )
                        }
                    }
                    is DailyLearningInitializationResult.IncompletePersonalization -> {
                        setState {
                            DailyFeedState.IncompletePersonalization(
                                reason = result.reason.message
                            )
                        }
                    }
                    is DailyLearningInitializationResult.ContentUnavailable -> {
                        setState {
                            DailyFeedState.ContentUnavailable(
                                reason = result.reason
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                setState { DailyFeedState.Error(e.message ?: "Unknown error occurred") }
                setEffect { DailyFeedEffect.ShowToast("Failed to load feed") }
            }
        }
    }
}
