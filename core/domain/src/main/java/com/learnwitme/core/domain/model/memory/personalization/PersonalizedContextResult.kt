package com.learnwitme.core.domain.model.memory.personalization

/**
 * Responsibility: Typed result hierarchy representing the outcome of initializing or building
 * a Personalized Learning Context. Avoids uncontrolled exceptions for expected validation or
 * incomplete profile states.
 */
sealed interface PersonalizedContextResult {

    /** Successful initialization containing the deterministic LearningPersonalizationContext. */
    data class Success(
        val context: LearningPersonalizationContext
    ) : PersonalizedContextResult

    /** Represents an incomplete or invalid user profile state preventing context construction. */
    sealed interface Incomplete : PersonalizedContextResult {
        val message: String

        data class MissingProfile(
            override val message: String = "UserProfile not found for specified user ID"
        ) : Incomplete

        data class EmptyPreferredTopics(
            override val message: String = "Learning preference has no preferred topics"
        ) : Incomplete

        data class InvalidDailyLimit(
            override val message: String = "Daily limit minutes must be greater than zero"
        ) : Incomplete

        data class InvalidLevelCombination(
            override val message: String = "Current difficulty level cannot be higher than target level"
        ) : Incomplete
    }
}
