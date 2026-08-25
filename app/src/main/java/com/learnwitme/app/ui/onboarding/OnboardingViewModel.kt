package com.learnwitme.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.InitializeGuestUserWorkflow
import com.learnwitme.core.domain.usecase.UpdateLearningPreferenceWorkflow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingState {
    object Idle : OnboardingState
    object Loading : OnboardingState
    object PreferenceStep : OnboardingState
    object Success : OnboardingState
    data class Error(val message: String) : OnboardingState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val initializeGuestUserWorkflow: InitializeGuestUserWorkflow,
    private val updateLearningPreferenceWorkflow: UpdateLearningPreferenceWorkflow
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state

    private val currentUserId = UserId("current_user")

    fun continueAsGuest() {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            try {
                initializeGuestUserWorkflow.execute()
                _state.value = OnboardingState.PreferenceStep
            } catch (e: Exception) {
                _state.value = OnboardingState.Error(e.message ?: "Failed to initialize guest profile")
            }
        }
    }

    fun savePreferences(
        preferredTopics: List<String>,
        dailyLimitMinutes: Int,
        targetLevel: DifficultyLevel = DifficultyLevel.MID
    ) {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading
            try {
                val careerPath = CareerPath(
                    code = "cp_kotlin_dev",
                    name = "Kotlin Developer",
                    description = "Build Android and Backend apps with Kotlin"
                )
                updateLearningPreferenceWorkflow.execute(
                    userId = currentUserId,
                    preferredTopics = preferredTopics,
                    dailyLimitMinutes = dailyLimitMinutes,
                    careerPath = careerPath,
                    currentLevel = DifficultyLevel.BEGINNER,
                    targetLevel = targetLevel
                )
                _state.value = OnboardingState.Success
            } catch (e: Exception) {
                _state.value = OnboardingState.Error(e.message ?: "Failed to save learning preferences")
            }
        }
    }

    fun startLearning() {
        continueAsGuest()
    }
}
