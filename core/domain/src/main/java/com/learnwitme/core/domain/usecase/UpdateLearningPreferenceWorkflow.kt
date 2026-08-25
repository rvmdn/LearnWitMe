package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.UserProfileRepository

/**
 * Responsibility: Handles domain workflow for updating learning preferences and career goals
 * on an existing UserProfile. Enforces domain invariants and validates preference input.
 */
class UpdateLearningPreferenceWorkflow(
    private val profileRepository: UserProfileRepository
) {
    suspend fun execute(
        userId: UserId,
        preferredTopics: List<String>,
        dailyLimitMinutes: Int,
        careerPath: CareerPath,
        currentLevel: DifficultyLevel,
        targetLevel: DifficultyLevel
    ): UserProfile {
        val existingProfile = profileRepository.getUserProfile(userId)
            ?: throw IllegalStateException("UserProfile not found for userId: ${userId.value}")

        require(preferredTopics.isNotEmpty()) { "Preferred topics cannot be empty" }
        require(dailyLimitMinutes > 0) { "Daily learning limit must be positive" }
        require(currentLevel.ordinal <= targetLevel.ordinal) {
            "Current difficulty level ($currentLevel) cannot be higher than target level ($targetLevel)"
        }

        val newPreference = LearningPreference(
            preferredTopics = preferredTopics,
            dailyLimitMinutes = dailyLimitMinutes
        )

        val updatedProfile = existingProfile
            .updatePreference(newPreference)
            .updateCareerGoal(
                newCareerPath = careerPath,
                newCurrentLevel = currentLevel,
                newTargetLevel = targetLevel
            )

        profileRepository.saveUserProfile(updatedProfile)
        return updatedProfile
    }
}
