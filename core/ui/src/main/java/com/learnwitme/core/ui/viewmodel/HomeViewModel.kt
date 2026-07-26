package com.learnwitme.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.application.service.HomeApplicationService
import com.learnwitme.core.application.state.UiState
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.usecase.DailyRecommendationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState : UiState {
    data object Loading : HomeUiState
    data class Success(
        val feedResult: DailyRecommendationResult? = null,
        val topRecommendation: Recommendation? = null,
        val userKnowledge: UserKnowledge? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeApplicationService: HomeApplicationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadDailyFeed(userId: UserId, availableCards: List<KnowledgeCard>, currentTimeMs: Long) {
        viewModelScope.launch {
            _uiState.update { HomeUiState.Loading }
            when (val result = homeApplicationService.generateDailyFeed(userId, availableCards, currentTimeMs)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        val existing = (current as? HomeUiState.Success) ?: HomeUiState.Success()
                        existing.copy(feedResult = result.data)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { HomeUiState.Error(result.message ?: "Failed to generate daily feed") }
                }
                is AppResult.Loading -> _uiState.update { HomeUiState.Loading }
            }
        }
    }

    fun loadRecommendations(userId: UserId) {
        viewModelScope.launch {
            when (val result = homeApplicationService.loadRecommendations(userId)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        val existing = (current as? HomeUiState.Success) ?: HomeUiState.Success()
                        existing.copy(topRecommendation = result.data)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { HomeUiState.Error(result.message ?: "Failed to load recommendations") }
                }
                is AppResult.Loading -> _uiState.update { HomeUiState.Loading }
            }
        }
    }

    fun loadLearningProgress(userId: UserId) {
        viewModelScope.launch {
            when (val result = homeApplicationService.loadLearningProgress(userId)) {
                is AppResult.Success -> {
                    _uiState.update { current ->
                        val existing = (current as? HomeUiState.Success) ?: HomeUiState.Success()
                        existing.copy(userKnowledge = result.data)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { HomeUiState.Error(result.message ?: "Failed to load learning progress") }
                }
                is AppResult.Loading -> _uiState.update { HomeUiState.Loading }
            }
        }
    }
}
