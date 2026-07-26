package com.learnwitme.core.domain.adaptive

/**
 * Single Responsibility: Pure stateless evaluator that evaluates a DifficultyProfile
 * against a DifficultyPolicy and outputs a deterministic DifficultyRecommendation.
 */
class DifficultyEvaluator {
    fun evaluateNextDifficulty(
        profile: DifficultyProfile,
        policy: DifficultyPolicy,
        currentTimeMs: Long
    ): DifficultyRecommendation {
        val currentLevel = profile.currentLevel

        // 1. Inactivity check
        if (profile.lastActivityTimestamp > 0 &&
            (currentTimeMs - profile.lastActivityTimestamp) > policy.inactivityThresholdMs
        ) {
            return DifficultyRecommendation(
                targetLevel = currentLevel,
                reason = DifficultyAdjustmentReason.LONG_INACTIVITY,
                explanation = "User inactive for over 7 days. Maintained level at ${currentLevel.name} for refresher."
            )
        }

        // 2. Repeated failures check -> Decrease
        if (profile.consecutiveFailuresCount >= policy.repeatedFailureThreshold) {
            val target = currentLevel.previousLevel()
            return DifficultyRecommendation(
                targetLevel = target,
                reason = DifficultyAdjustmentReason.REPEATED_FAILURES,
                explanation = "Detected ${profile.consecutiveFailuresCount} consecutive failures. Decreased difficulty to ${target.name}."
            )
        }

        // 3. Low accuracy check -> Decrease
        if (profile.overallAccuracy <= policy.lowAccuracyThreshold && profile.totalSessionsCompleted > 0) {
            val target = currentLevel.previousLevel()
            return DifficultyRecommendation(
                targetLevel = target,
                reason = DifficultyAdjustmentReason.LOW_ACCURACY,
                explanation = "Accuracy fell to ${(profile.overallAccuracy * 100).toInt()}%. Decreased difficulty to ${target.name}."
            )
        }

        // 4. High mastery check -> Increase
        if (profile.masteryPercentage >= policy.masteryThreshold) {
            val target = currentLevel.nextLevel()
            return DifficultyRecommendation(
                targetLevel = target,
                reason = DifficultyAdjustmentReason.MASTERED_TOPIC,
                explanation = "Mastery reached ${(profile.masteryPercentage * 100).toInt()}%. Promoted difficulty to ${target.name}."
            )
        }

        // 5. High accuracy check -> Increase
        if (profile.overallAccuracy >= policy.highAccuracyThreshold && profile.totalSessionsCompleted > 0) {
            val target = currentLevel.nextLevel()
            return DifficultyRecommendation(
                targetLevel = target,
                reason = DifficultyAdjustmentReason.HIGH_ACCURACY,
                explanation = "High accuracy of ${(profile.overallAccuracy * 100).toInt()}%. Promoted difficulty to ${target.name}."
            )
        }

        // 6. Default consistent progress
        return DifficultyRecommendation(
            targetLevel = currentLevel,
            reason = DifficultyAdjustmentReason.CONSISTENT_PROGRESS,
            explanation = "Performance stable. Maintained difficulty level at ${currentLevel.name}."
        )
    }
}
