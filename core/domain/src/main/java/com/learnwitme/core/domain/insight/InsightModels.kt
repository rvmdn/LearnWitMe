package com.learnwitme.core.domain.insight

/**
 * High-level trend indicators for learner performance.
 */
enum class LearningTrend {
    UPWARD_TREND,
    DOWNWARD_TREND,
    STABLE_PROGRESS,
    REVIEW_OVERDUE,
    MASTERY_GROWTH,
    LONG_INACTIVITY
}

/**
 * Granular skill insight item indicating competency level and status.
 */
data class SkillInsight(
    val skillCode: String,
    val skillName: String,
    val competencyScore: Double,
    val isWeak: Boolean,
    val isStrong: Boolean
)

/**
 * Summary of weekly learner engagement statistics.
 */
data class WeeklyLearningSummary(
    val totalCardsStudied: Int,
    val totalReviewsCompleted: Int,
    val activeDaysCount: Int,
    val averageAccuracy: Double
)

/**
 * Policy thresholds governing insight trend calculations.
 */
data class LearningInsightPolicy(
    val upwardTrendThreshold: Double = 0.85,
    val downwardTrendThreshold: Double = 0.50,
    val weakSkillThreshold: Double = 0.60,
    val strongSkillThreshold: Double = 0.90
)

/**
 * Aggregated explainable insight object presented to the learner.
 */
data class LearningInsight(
    val trend: LearningTrend,
    val title: String,
    val description: String,
    val strongestSkills: List<SkillInsight>,
    val weakestSkills: List<SkillInsight>,
    val weeklySummary: WeeklyLearningSummary
)
