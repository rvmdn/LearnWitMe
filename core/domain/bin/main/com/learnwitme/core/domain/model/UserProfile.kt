package com.learnwitme.core.domain.model

/**
 * Responsibility: Serves as the Aggregate Root representing user identification, career choice,
 * learning preferences, and current learning metrics/progress.
 */
data class UserProfile(
    override val id: UserId,
    val careerPath: CareerPath,
    val preference: LearningPreference,
    val currentLevel: DifficultyLevel,
    val targetLevel: DifficultyLevel,
    val progress: LearningProgress
) : AggregateRoot<UserId> {

    init {
        // Validate level invariant
        require(currentLevel.ordinal <= targetLevel.ordinal) {
            "Current difficulty level ($currentLevel) cannot be higher than target level ($targetLevel)"
        }
    }

    /**
     * Updates learning preferences, returning a new immutable UserProfile instance.
     */
    fun updatePreference(newPreference: LearningPreference): UserProfile {
        return this.copy(preference = newPreference)
    }

    /**
     * Changes the user's career path and resets target level to MID/SENIOR accordingly.
     */
    fun updateCareerGoal(
        newCareerPath: CareerPath,
        newCurrentLevel: DifficultyLevel,
        newTargetLevel: DifficultyLevel
    ): UserProfile {
        require(newCurrentLevel.ordinal <= newTargetLevel.ordinal) {
            "New current level ($newCurrentLevel) cannot be higher than new target level ($newTargetLevel)"
        }
        return this.copy(
            careerPath = newCareerPath,
            currentLevel = newCurrentLevel,
            targetLevel = newTargetLevel
        )
    }

    /**
     * Updates the current progress metrics safely.
     */
    fun updateProgress(newProgress: LearningProgress): UserProfile {
        return this.copy(progress = newProgress)
    }
}
