package com.learnwitme.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.usecase.LoadUserProfileWorkflow
import com.learnwitme.core.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loadUserProfileWorkflow: LoadUserProfileWorkflow
) : BaseViewModel<ProfileState, ProfileEvent, ProfileEffect>() {

    private val currentUserId = UserId("current_user")

    init {
        loadProfile()
    }

    override fun createInitialState(): ProfileState = ProfileState.Loading

    override fun handleEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.OnLoadProfile -> loadProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            setState { ProfileState.Loading }
            try {
                val profile = loadUserProfileWorkflow.execute(currentUserId)
                if (profile != null) {
                    setState { ProfileState.Success(profile) }
                } else {
                    setState { ProfileState.Error("Profile not found") }
                }
            } catch (e: Exception) {
                setState { ProfileState.Error(e.message ?: "Failed to load profile") }
            }
        }
    }
}
