package com.learnwitme.core.domain.model

import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Responsibility: Explicit, typed domain model representing the learner's state at app startup.
 * Determines whether the user should start onboarding as a new guest, complete missing
 * preferences, or proceed directly to the personalized learning feed.
 */
sealed interface UserStartupState {

    /** Fresh guest user with no persisted profile; must enter welcome & guest setup. */
    data class NewGuest(
        val userId: UserId = UserIdentity.Guest.DEFAULT_GUEST_ID
    ) : UserStartupState

    /** Profile exists, but learning preferences (topics, daily limit) must be configured. */
    data class PreferenceSetupRequired(
        val profile: UserProfile
    ) : UserStartupState

    /** Profile and preferences are fully configured; ready to proceed to personalized learning. */
    data class Ready(
        val profile: UserProfile,
        val context: LearningPersonalizationContext
    ) : UserStartupState
}
