package com.learnwitme.core.domain.progress

/**
 * Tracks consecutive study day streaks.
 */
data class LearningStreak(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastStudyDateTimestamp: Long
)

/**
 * Detailed statistical breakdown of learner activity.
 */
data class LearningStatistics(
    val cardsLearned: Int,
    val cardsReviewed: Int,
    val reviewsDue: Int,
    val averageAccuracy: Double
)

/**
 * Aggregate summary model presented on the user progress dashboard.
 */
data class LearningProgressSummary(
    val currentLevel: Int,
    val totalXP: Int,
    val totalStudyTimeSeconds: Long,
    val completionRatePercentage: Double,
    val masteryRatePercentage: Double,
    val activeCoursesCount: Int,
    val streak: LearningStreak,
    val statistics: LearningStatistics
)
