package com.learnwitme.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingState {
    object Idle : OnboardingState
    object Loading : OnboardingState
    object Success : OnboardingState
    data class Error(val message: String) : OnboardingState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state

    fun startLearning() {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            try {
                val userId = UserId("current_user")
                val defaultProfile = UserProfile(
                    id = userId,
                    careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Build Android and Backend apps with Kotlin"),
                    preference = LearningPreference(preferredTopics = listOf("tp_kotlin"), dailyLimitMinutes = 30),
                    currentLevel = DifficultyLevel.BEGINNER,
                    targetLevel = DifficultyLevel.MID,
                    progress = LearningProgress(totalUnitsCompleted = 0, totalCardsMastered = 0, activeStreakDays = 0, lastActiveDate = 0L)
                )
                profileRepository.saveUserProfile(defaultProfile)
                _state.value = OnboardingState.Success
            } catch (e: Exception) {
                _state.value = OnboardingState.Error(e.message ?: "Failed to create profile")
            }
        }
    }
}
