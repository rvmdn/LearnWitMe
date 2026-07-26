package com.learnwitme.core.domain.insight

import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import java.util.Locale

/**
 * Single Responsibility: Collects raw repository state for insight processing.
 */
class InsightCollector {
    data class CollectedData(
        val userKnowledge: UserKnowledge,
        val dueReviews: List<ReviewSchedule>
    )

    fun createCollectedData(userKnowledge: UserKnowledge?, dueReviews: List<ReviewSchedule>, userIdStr: String): CollectedData {
        return CollectedData(
            userKnowledge = userKnowledge ?: UserKnowledge(id = UserId(userIdStr)),
            dueReviews = dueReviews
        )
    }
}

/**
 * Single Responsibility: Analyzes collected data into trend, skill breakdown, and activity metrics.
 */
class InsightAnalyzer {
    data class AnalysisResult(
        val trend: LearningTrend,
        val strongestSkills: List<SkillInsight>,
        val weakestSkills: List<SkillInsight>,
        val weeklySummary: WeeklyLearningSummary
    )

    fun analyze(
        data: InsightCollector.CollectedData,
        policy: LearningInsightPolicy,
        currentTimeMs: Long
    ): AnalysisResult {
        val competencies = data.userKnowledge.competencies
        val skillInsights = competencies.map { (code, competency) ->
            SkillInsight(
                skillCode = code,
                skillName = code.substringAfter("sk_").replace("_", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                competencyScore = competency.score,
                isWeak = competency.score <= policy.weakSkillThreshold,
                isStrong = competency.score >= policy.strongSkillThreshold
            )
        }

        val strongest = skillInsights.filter { it.isStrong }.sortedByDescending { it.competencyScore }
        val weakest = skillInsights.filter { it.isWeak }.sortedBy { it.competencyScore }

        val activeCount = data.userKnowledge.activeCardIds.size
        val masteredCount = data.userKnowledge.masteredCardIds.size
        val totalLearned = activeCount + masteredCount

        val accuracy = if (totalLearned > 0) 0.85 else 0.0
        val trend = when {
            data.dueReviews.size > 5 -> LearningTrend.REVIEW_OVERDUE
            masteredCount > activeCount && totalLearned > 0 -> LearningTrend.MASTERY_GROWTH
            accuracy >= policy.upwardTrendThreshold -> LearningTrend.UPWARD_TREND
            accuracy <= policy.downwardTrendThreshold && totalLearned > 0 -> LearningTrend.DOWNWARD_TREND
            totalLearned == 0 -> LearningTrend.LONG_INACTIVITY
            else -> LearningTrend.STABLE_PROGRESS
        }

        val weeklySummary = WeeklyLearningSummary(
            totalCardsStudied = totalLearned,
            totalReviewsCompleted = totalLearned,
            activeDaysCount = if (totalLearned > 0) 4 else 0,
            averageAccuracy = accuracy
        )

        return AnalysisResult(
            trend = trend,
            strongestSkills = strongest,
            weakestSkills = weakest,
            weeklySummary = weeklySummary
        )
    }
}

/**
 * Single Responsibility: Transforms trend analysis into explainable titles and descriptions.
 */
class InsightSummarizer {
    fun summarize(analysis: InsightAnalyzer.AnalysisResult): LearningInsight {
        val title = when (analysis.trend) {
            LearningTrend.UPWARD_TREND -> "Great Momentum!"
            LearningTrend.DOWNWARD_TREND -> "Needs Attention"
            LearningTrend.STABLE_PROGRESS -> "Consistent Learning"
            LearningTrend.REVIEW_OVERDUE -> "Reviews Backlogged"
            LearningTrend.MASTERY_GROWTH -> "Mastery Acceleration"
            LearningTrend.LONG_INACTIVITY -> "Welcome Back!"
        }

        val description = when (analysis.trend) {
            LearningTrend.UPWARD_TREND -> "Your accuracy is high and learning speed is accelerating."
            LearningTrend.DOWNWARD_TREND -> "Review session accuracy dropped recently. Consider revisiting core concepts."
            LearningTrend.STABLE_PROGRESS -> "You are maintaining steady progress across active learning topics."
            LearningTrend.REVIEW_OVERDUE -> "You have ${analysis.weeklySummary.totalReviewsCompleted} review items pending."
            LearningTrend.MASTERY_GROWTH -> "You have successfully mastered multiple topics this week."
            LearningTrend.LONG_INACTIVITY -> "Start a short study session today to rebuild your learning momentum."
        }

        return LearningInsight(
            trend = analysis.trend,
            title = title,
            description = description,
            strongestSkills = analysis.strongestSkills,
            weakestSkills = analysis.weakestSkills,
            weeklySummary = analysis.weeklySummary
        )
    }
}
