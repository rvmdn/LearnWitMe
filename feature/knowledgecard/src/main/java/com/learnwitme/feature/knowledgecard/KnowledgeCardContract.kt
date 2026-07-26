package com.learnwitme.feature.knowledgecard

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface KnowledgeCardState : UiState {
    object Loading : KnowledgeCardState
    data class Success(val card: KnowledgeCard) : KnowledgeCardState
    data class Error(val message: String) : KnowledgeCardState
}

sealed interface KnowledgeCardEvent : UiEvent {
    data class OnLoadCard(val cardId: String) : KnowledgeCardEvent
    data class OnSubmitReview(val cardId: String, val rating: Double) : KnowledgeCardEvent
}

sealed interface KnowledgeCardEffect : UiEffect {
    data class ShowToast(val message: String) : KnowledgeCardEffect
    object NavigateBack : KnowledgeCardEffect
}
