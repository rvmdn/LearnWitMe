package com.learnwitme.feature.dailyfeed

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.usecase.BuildDailyFeedWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyFeedViewModel @Inject constructor(
    private val buildDailyFeedWorkflow: BuildDailyFeedWorkflow
) : BaseViewModel<DailyFeedState, DailyFeedEvent, DailyFeedEffect>() {

    private val currentUserId = UserId("current_user")

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
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            setState { DailyFeedState.Loading }
            try {
                val feed = buildDailyFeedWorkflow.execute(currentUserId)
                setState { DailyFeedState.Success(feed) }
            } catch (e: Exception) {
                setState { DailyFeedState.Error(e.message ?: "Unknown error occurred") }
                setEffect { DailyFeedEffect.ShowToast("Failed to load feed") }
            }
        }
    }
}
