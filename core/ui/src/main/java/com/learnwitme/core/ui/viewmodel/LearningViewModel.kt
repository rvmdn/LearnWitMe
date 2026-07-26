package com.learnwitme.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.application.service.LearningApplicationService
import com.learnwitme.core.application.state.UiState
import com.learnwitme.core.domain.event.ReviewCompletedEvent
import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LearningUiState : UiState {
    data object Idle : LearningUiState
    data object Loading : LearningUiState
    data class ReviewSuccess(val event: ReviewCompletedEvent) : LearningUiState
    data object ActionSuccess : LearningUiState
    data class Error(val message: String) : LearningUiState
}

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningApplicationService: LearningApplicationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Idle)
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    fun startLearning(userId: UserId, cardId: KnowledgeCardId, currentTimeMs: Long) {
        viewModelScope.launch {
            _uiState.update { LearningUiState.Loading }
            when (val result = learningApplicationService.startLearning(userId, cardId, currentTimeMs)) {
                is AppResult.Success -> _uiState.update { LearningUiState.ActionSuccess }
                is AppResult.Error -> _uiState.update { LearningUiState.Error(result.message ?: "Failed to start learning") }
                is AppResult.Loading -> _uiState.update { LearningUiState.Loading }
            }
        }
    }

    fun completeReview(userId: UserId, cardId: KnowledgeCardId, qualityScore: Double, currentTimeMs: Long) {
        viewModelScope.launch {
            _uiState.update { LearningUiState.Loading }
            when (val result = learningApplicationService.completeReview(userId, cardId, qualityScore, currentTimeMs)) {
                is AppResult.Success -> _uiState.update { LearningUiState.ReviewSuccess(result.data) }
                is AppResult.Error -> _uiState.update { LearningUiState.Error(result.message ?: "Failed to record review") }
                is AppResult.Loading -> _uiState.update { LearningUiState.Loading }
            }
        }
    }

    fun updateCompetency(userId: UserId, skill: Skill, competency: Competency) {
        viewModelScope.launch {
            _uiState.update { LearningUiState.Loading }
            when (val result = learningApplicationService.updateCompetency(userId, skill, competency)) {
                is AppResult.Success -> _uiState.update { LearningUiState.ActionSuccess }
                is AppResult.Error -> _uiState.update { LearningUiState.Error(result.message ?: "Failed to update competency") }
                is AppResult.Loading -> _uiState.update { LearningUiState.Loading }
            }
        }
    }
}
