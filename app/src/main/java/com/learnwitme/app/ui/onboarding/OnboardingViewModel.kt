package com.learnwitme.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.usecase.InitializeGuestUserWorkflow
import com.learnwitme.core.domain.usecase.InitializePersonalizedDailyLearningWorkflow
import com.learnwitme.core.domain.usecase.UpdateLearningPreferenceWorkflow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingState {
    object Idle : OnboardingState
    data class Loading(val message: String? = null) : OnboardingState
    data class PreferenceStep(val error: String? = null) : OnboardingState
    data class InitializingPersonalization(val profile: UserProfile) : OnboardingState
    data class Success(val initializationResult: DailyLearningInitializationResult.Success) : OnboardingState
    data class Error(val message: String, val canRetryPreferences: Boolean = false) : OnboardingState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val initializeGuestUserWorkflow: InitializeGuestUserWorkflow,
    private val updateLearningPreferenceWorkflow: UpdateLearningPreferenceWorkflow,
    private val initializePersonalizedDailyLearningWorkflow: InitializePersonalizedDailyLearningWorkflow,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val state: StateFlow<OnboardingState> = _state

    private val currentUserId = UserIdentity.Guest.DEFAULT_GUEST_ID

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            try {
                val profile = profileRepository.getUserProfile(currentUserId)
                if (profile != null && profile.preference.preferredTopics.isEmpty()) {
                    _state.value = OnboardingState.PreferenceStep()
                } else if (profile == null) {
                    _state.value = OnboardingState.Idle
                }
            } catch (e: Exception) {
                _state.value = OnboardingState.Idle
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading("Initializing guest session...")
            try {
                initializeGuestUserWorkflow.execute()
                _state.value = OnboardingState.PreferenceStep()
            } catch (e: Exception) {
                _state.value = OnboardingState.Error(
                    message = e.message ?: "Failed to initialize guest profile",
                    canRetryPreferences = false
                )
            }
        }
    }

    fun savePreferences(
        preferredTopics: List<String>,
        dailyLimitMinutes: Int,
        targetLevel: DifficultyLevel = DifficultyLevel.MID
    ) {
        viewModelScope.launch {
            _state.value = OnboardingState.Loading("Saving learning preferences...")
            try {
                val careerPath = CareerPath(
                    code = "cp_kotlin_dev",
                    name = "Kotlin Developer",
                    description = "Build Android and Backend apps with Kotlin"
                )
                val updatedProfile = updateLearningPreferenceWorkflow.execute(
                    userId = currentUserId,
                    preferredTopics = preferredTopics,
                    dailyLimitMinutes = dailyLimitMinutes,
                    careerPath = careerPath,
                    currentLevel = DifficultyLevel.BEGINNER,
                    targetLevel = targetLevel
                )

                _state.value = OnboardingState.InitializingPersonalization(updatedProfile)

                val initResult = initializePersonalizedDailyLearningWorkflow.execute(currentUserId)
                when (initResult) {
                    is DailyLearningInitializationResult.Success -> {
                        _state.value = OnboardingState.Success(initResult)
                    }
                    is DailyLearningInitializationResult.IncompletePersonalization -> {
                        _state.value = OnboardingState.PreferenceStep(
                            error = "Personalization incomplete: ${initResult.reason.message}"
                        )
                    }
                    is DailyLearningInitializationResult.ContentUnavailable -> {
                        _state.value = OnboardingState.Error(
                            message = "Learning content unavailable: ${initResult.reason}",
                            canRetryPreferences = true
                        )
                    }
                }
            } catch (e: IllegalArgumentException) {
                _state.value = OnboardingState.PreferenceStep(
                    error = e.message ?: "Invalid learning preferences"
                )
            } catch (e: Exception) {
                _state.value = OnboardingState.Error(
                    message = e.message ?: "Failed to save preferences and initialize learning",
                    canRetryPreferences = true
                )
            }
        }
    }

    fun startLearning() {
        continueAsGuest()
    }
}
