package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.UserId

/**
 * 5-tier adaptive difficulty ladder for progressive learner content scaling.
 */
enum class AdaptiveDifficultyLevel {
    BEGINNER,
    EASY,
    MEDIUM,
    HARD,
    EXPERT;

    fun nextLevel(): AdaptiveDifficultyLevel = when (this) {
        BEGINNER -> EASY
        EASY -> MEDIUM
        MEDIUM -> HARD
        HARD -> EXPERT
        EXPERT -> EXPERT
    }

    fun previousLevel(): AdaptiveDifficultyLevel = when (this) {
        EXPERT -> HARD
        HARD -> MEDIUM
        MEDIUM -> EASY
        EASY -> BEGINNER
        BEGINNER -> BEGINNER
    }
}

/**
 * Audit reasons explaining why a difficulty level was adjusted or maintained.
 */
enum class DifficultyAdjustmentReason {
    HIGH_ACCURACY,
    LOW_ACCURACY,
    FAST_PROGRESS,
    REPEATED_FAILURES,
    MASTERED_TOPIC,
    LONG_INACTIVITY,
    CONSISTENT_PROGRESS
}

/**
 * Immutable historical performance snapshot of a learner.
 */
data class DifficultyProfile(
    val userId: UserId,
    val currentLevel: AdaptiveDifficultyLevel = AdaptiveDifficultyLevel.BEGINNER,
    val totalSessionsCompleted: Int = 0,
    val overallAccuracy: Double = 0.0,
    val masteryPercentage: Double = 0.0,
    val consecutiveHighAccuracyCount: Int = 0,
    val consecutiveFailuresCount: Int = 0,
    val lastActivityTimestamp: Long = 0L
)

/**
 * Output recommendation produced by the difficulty evaluation engine.
 */
data class DifficultyRecommendation(
    val targetLevel: AdaptiveDifficultyLevel,
    val reason: DifficultyAdjustmentReason,
    val explanation: String
)

/**
 * Configurable business rules governing difficulty promotion and demotion thresholds.
 */
data class DifficultyPolicy(
    val highAccuracyThreshold: Double = 0.90,
    val lowAccuracyThreshold: Double = 0.50,
    val masteryThreshold: Double = 0.95,
    val repeatedFailureThreshold: Int = 3,
    val inactivityThresholdMs: Long = 7 * 24 * 60 * 60 * 1000L
)
