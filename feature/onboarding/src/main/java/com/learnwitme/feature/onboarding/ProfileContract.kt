package com.learnwitme.feature.onboarding

import com.learnwitme.core.domain.model.UserProfile
import com.learnwitme.core.ui.mvi.UiEffect
import com.learnwitme.core.ui.mvi.UiEvent
import com.learnwitme.core.ui.mvi.UiState

sealed interface ProfileState : UiState {
    object Loading : ProfileState
    data class Success(val profile: UserProfile) : ProfileState
    data class Error(val message: String) : ProfileState
}

sealed interface ProfileEvent : UiEvent {
    object OnLoadProfile : ProfileEvent
}

sealed interface ProfileEffect : UiEffect {
    data class ShowToast(val message: String) : ProfileEffect
}
