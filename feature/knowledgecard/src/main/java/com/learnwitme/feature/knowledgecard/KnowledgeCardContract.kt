package com.learnwitme.feature.knowledgecard

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface AiAssistanceState {
    object Idle : AiAssistanceState
    object Loading : AiAssistanceState
    data class Success(
        val interactionId: String,
        val explanation: String,
        val feedbackSubmitted: Boolean = false
    ) : AiAssistanceState
    data class Error(val message: String) : AiAssistanceState
}

sealed interface KnowledgeCardState : UiState {
    object Loading : KnowledgeCardState
    data class Success(
        val card: KnowledgeCard,
        val aiState: AiAssistanceState = AiAssistanceState.Idle
    ) : KnowledgeCardState
    data class Error(val message: String) : KnowledgeCardState
}

sealed interface KnowledgeCardEvent : UiEvent {
    data class OnLoadCard(val cardId: String) : KnowledgeCardEvent
    data class OnSubmitReview(val cardId: String, val rating: Double) : KnowledgeCardEvent
    data class OnRequestAiExplanation(val cardId: String) : KnowledgeCardEvent
    data class OnSubmitAiFeedback(val interactionId: String, val rating: Double, val comment: String? = null) : KnowledgeCardEvent
    object OnDismissAiExplanation : KnowledgeCardEvent
}

sealed interface KnowledgeCardEffect : UiEffect {
    data class ShowToast(val message: String) : KnowledgeCardEffect
    object NavigateBack : KnowledgeCardEffect
}
