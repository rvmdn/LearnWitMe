package com.learnwitme.core.domain.model

data class LearningSession(
    override val id: LearningSessionId,
    val startTime: Long,
    val endTime: Long,
    val completedUnitIds: List<LearningUnitId>
) : Entity<LearningSessionId> {
    init {
        require(startTime > 0) { "Start time must be positive" }
        require(endTime >= startTime) { "End time cannot be before start time" }
    }
    
    val durationMinutes: Double
        get() = (endTime - startTime).toDouble() / (1000 * 60)
}

data class ProgressSummary(
    val completionPercentage: Double,
    val masteryPercentage: Double,
    val currentStreak: Int,
    val dailyProgressUnits: Int,
    val weeklyProgressUnits: Int,
    val learningVelocityUnitsPerMinute: Double
) : ValueObject {
    init {
        require(completionPercentage in 0.0..1.0) { "Completion must be between 0.0 and 1.0" }
        require(masteryPercentage in 0.0..1.0) { "Mastery must be between 0.0 and 1.0" }
        require(currentStreak >= 0) { "Streak cannot be negative" }
        require(dailyProgressUnits >= 0) { "Daily progress cannot be negative" }
        require(weeklyProgressUnits >= 0) { "Weekly progress cannot be negative" }
        require(learningVelocityUnitsPerMinute >= 0.0) { "Velocity cannot be negative" }
    }
}

data class ProgressPolicy(
    val streakResetThresholdMillis: Long = 48L * 60 * 60 * 1000L, // 48 hours
    val targetDailyUnits: Int = 3
) : ValueObject

data class ProgressDecision(
    val updatedProfile: UserProfile,
    val summary: ProgressSummary
) : ValueObject
