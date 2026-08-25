package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserIdentity
import com.learnwitme.core.domain.model.UserStartupState
import com.learnwitme.core.domain.model.memory.personalization.PersonalizedContextResult
import com.learnwitme.core.domain.repository.UserProfileRepository

/**
 * Responsibility: Evaluates the learner's persisted profile and preferences at app startup
 * to deterministically decide whether the user is a new guest, requires preference setup,
 * or is ready for the personalized learning experience.
 */
class DetermineUserStartupStateWorkflow(
    private val profileRepository: UserProfileRepository,
    private val buildPersonalizedLearningContextWorkflow: BuildPersonalizedLearningContextWorkflow
) {
    suspend fun execute(userId: UserId = UserIdentity.Guest.DEFAULT_GUEST_ID): UserStartupState {
        val profile = profileRepository.getUserProfile(userId)
            ?: return UserStartupState.NewGuest(userId)

        val contextResult = buildPersonalizedLearningContextWorkflow.execute(userId)
        return when (contextResult) {
            is PersonalizedContextResult.Success -> {
                UserStartupState.Ready(profile, contextResult.context)
            }
            is PersonalizedContextResult.Incomplete -> {
                UserStartupState.PreferenceSetupRequired(profile)
            }
        }
    }
}
