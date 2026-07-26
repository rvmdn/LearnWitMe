package com.learnwitme.app.ui.startup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learnwitme.app.seeder.DefaultPackageParser
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.calculateChecksum
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.usecase.InitializeApplicationWorkflow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.learnwitme.core.config.LocalizationManager

sealed interface StartupState {
    object Loading : StartupState
    object OnboardingRequired : StartupState
    object Success : StartupState
    data class Error(val message: String) : StartupState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val initializeApplicationWorkflow: InitializeApplicationWorkflow,
    private val profileRepository: UserProfileRepository,
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
                val userId = UserId("current_user")
                val activeLang = localizationManager.currentLanguage.value
                initializeApplicationWorkflow.execute(userId) {
                    val parsed = DefaultPackageParser.parse(context, activeLang)
                    val correctChecksum = calculateChecksum(parsed)
                    parsed.copy(metadata = parsed.metadata.copy(checksum = correctChecksum))
                }
                
                val profile = profileRepository.getUserProfile(userId)
                if (profile == null) {
                    _state.value = StartupState.OnboardingRequired
                } else {
                    _state.value = StartupState.Success
                }
            } catch (e: Exception) {
                _state.value = StartupState.Error(e.message ?: "Failed to initialize application")
            }
        }
    }
}
