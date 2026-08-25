package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.repository.UserProfileRepository

/**
 * Responsibility: Handles domain workflow for creating/initializing local guest user identity and profile.
 * Ensures deterministic guest identity persistence without duplicating profiles or overwriting existing user state.
 */
class InitializeGuestUserWorkflow(
    private val profileRepository: UserProfileRepository
) {
    suspend fun execute(identity: UserIdentity.Guest = UserIdentity.Guest()): UserProfile {
        val existingProfile = profileRepository.getUserProfile(identity.id)
        if (existingProfile != null) {
            return existingProfile
        }

        val guestProfile = UserProfile(
            id = identity.id,
            careerPath = CareerPath(
                code = "cp_kotlin_dev",
                name = "Kotlin Developer",
                description = "Build Android and Backend apps with Kotlin"
            ),
            preference = LearningPreference(
                preferredTopics = listOf("tp_kotlin"),
                dailyLimitMinutes = 30
            ),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(
                totalUnitsCompleted = 0,
                totalCardsMastered = 0,
                activeStreakDays = 0,
                lastActiveDate = 0L
            )
        )

        profileRepository.saveUserProfile(guestProfile)
        return guestProfile
    }
}
