package com.learnwitme.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.application.service.KnowledgeApplicationService
import com.learnwitme.core.application.state.UiState
import com.learnwitme.core.domain.event.KnowledgePackageImportedEvent
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.usecase.KnowledgeCardDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface KnowledgeUiState : UiState {
    data object Idle : KnowledgeUiState
    data object Loading : KnowledgeUiState
    data class CardSearchResults(val cards: List<KnowledgeCard>) : KnowledgeUiState
    data class CardDetailsLoaded(val details: KnowledgeCardDetails?) : KnowledgeUiState
    data class PackageImported(val event: KnowledgePackageImportedEvent) : KnowledgeUiState
    data class PackagePublished(val pkg: KnowledgePackage) : KnowledgeUiState
    data class Error(val message: String) : KnowledgeUiState
}

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val knowledgeApplicationService: KnowledgeApplicationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<KnowledgeUiState>(KnowledgeUiState.Idle)
    val uiState: StateFlow<KnowledgeUiState> = _uiState.asStateFlow()

    fun searchCards(skills: List<Skill>) {
        viewModelScope.launch {
            _uiState.update { KnowledgeUiState.Loading }
            when (val result = knowledgeApplicationService.searchCardsBySkills(skills)) {
                is AppResult.Success -> _uiState.update { KnowledgeUiState.CardSearchResults(result.data) }
                is AppResult.Error -> _uiState.update { KnowledgeUiState.Error(result.message ?: "Search failed") }
                is AppResult.Loading -> _uiState.update { KnowledgeUiState.Loading }
            }
        }
    }

    fun loadCardDetails(userId: UserId, cardId: KnowledgeCardId) {
        viewModelScope.launch {
            _uiState.update { KnowledgeUiState.Loading }
            when (val result = knowledgeApplicationService.findCardDetails(userId, cardId)) {
                is AppResult.Success -> _uiState.update { KnowledgeUiState.CardDetailsLoaded(result.data) }
                is AppResult.Error -> _uiState.update { KnowledgeUiState.Error(result.message ?: "Failed to load card details") }
                is AppResult.Loading -> _uiState.update { KnowledgeUiState.Loading }
            }
        }
    }

    fun importPackage(pkg: KnowledgePackage, currentTimeMs: Long) {
        viewModelScope.launch {
            _uiState.update { KnowledgeUiState.Loading }
            when (val result = knowledgeApplicationService.importPackage(pkg, currentTimeMs)) {
                is AppResult.Success -> _uiState.update { KnowledgeUiState.PackageImported(result.data) }
                is AppResult.Error -> _uiState.update { KnowledgeUiState.Error(result.message ?: "Import failed") }
                is AppResult.Loading -> _uiState.update { KnowledgeUiState.Loading }
            }
        }
    }

    fun publishPackage(packageId: KnowledgePackageId) {
        viewModelScope.launch {
            _uiState.update { KnowledgeUiState.Loading }
            when (val result = knowledgeApplicationService.publishPackage(packageId)) {
                is AppResult.Success -> _uiState.update { KnowledgeUiState.PackagePublished(result.data) }
                is AppResult.Error -> _uiState.update { KnowledgeUiState.Error(result.message ?: "Publishing failed") }
                is AppResult.Loading -> _uiState.update { KnowledgeUiState.Loading }
            }
        }
    }
}
