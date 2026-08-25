package com.learnwitme.app.ui.startup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.app.seeder.DefaultPackageParser
import com.learnwitme.core.config.LocalizationManager
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.usecase.DetermineUserStartupStateWorkflow
import com.learnwitme.core.domain.usecase.InitializeApplicationWorkflow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartupState {
    object Loading : StartupState
    data class NewGuestOnboarding(val userId: UserId) : StartupState
    data class PreferenceSetupRequired(val profile: UserProfile) : StartupState
    data class Ready(val profile: UserProfile, val context: LearningPersonalizationContext) : StartupState
    data class Error(val message: String) : StartupState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val initializeApplicationWorkflow: InitializeApplicationWorkflow,
    private val determineUserStartupStateWorkflow: DetermineUserStartupStateWorkflow,
    private val localizationManager: LocalizationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state: StateFlow<StartupState> = _state

    init {
        initialize()
    }

    fun initialize() {
        viewModelScope.launch {
            _state.value = StartupState.Loading
            try {
                val userId = UserIdentity.Guest.DEFAULT_GUEST_ID
                val activeLang = localizationManager.currentLanguage.value
                initializeApplicationWorkflow.execute(userId) {
                    val parsed = DefaultPackageParser.parse(context, activeLang)
                    val correctChecksum = calculateChecksum(parsed)
                    parsed.copy(metadata = parsed.metadata.copy(checksum = correctChecksum))
                }

                val startupDecision = determineUserStartupStateWorkflow.execute(userId)
                _state.value = when (startupDecision) {
                    is UserStartupState.NewGuest -> StartupState.NewGuestOnboarding(startupDecision.userId)
                    is UserStartupState.PreferenceSetupRequired -> StartupState.PreferenceSetupRequired(startupDecision.profile)
                    is UserStartupState.Ready -> StartupState.Ready(startupDecision.profile, startupDecision.context)
                }
            } catch (e: Exception) {
                _state.value = StartupState.Error(e.message ?: "Failed to initialize application")
            }
        }
    }
}
