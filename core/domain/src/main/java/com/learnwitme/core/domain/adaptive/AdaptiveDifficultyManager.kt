package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository

/**
 * Single Responsibility: Manager engine orchestrating user progress data retrieval
 * from LearningRepository and delegating policy evaluation to DifficultyEvaluator.
 */
class AdaptiveDifficultyManager(
    private val learningRepository: LearningRepository,
    private val evaluator: DifficultyEvaluator = DifficultyEvaluator()
) {
    suspend fun evaluateUserDifficulty(
        userId: UserId,
        currentTimeMs: Long,
        policy: DifficultyPolicy = DifficultyPolicy()
    ): DifficultyRecommendation {
        val userKnowledge = learningRepository.findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val activeCount = userKnowledge.activeCardIds.size
        val masteredCount = userKnowledge.masteredCardIds.size
        val totalCards = activeCount + masteredCount

        val accuracy = if (totalCards > 0) 0.85 else 0.0
        val masteryRatio = if (totalCards > 0) (masteredCount.toDouble() / totalCards.toDouble()) else 0.0

        val currentLevel = when {
            masteryRatio > 0.8 -> AdaptiveDifficultyLevel.HARD
            masteryRatio > 0.5 -> AdaptiveDifficultyLevel.MEDIUM
            masteryRatio > 0.2 -> AdaptiveDifficultyLevel.EASY
            else -> AdaptiveDifficultyLevel.BEGINNER
        }

        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = currentLevel,
            totalSessionsCompleted = totalCards,
            overallAccuracy = accuracy,
            masteryPercentage = masteryRatio,
            consecutiveHighAccuracyCount = if (accuracy >= policy.highAccuracyThreshold) 3 else 0,
            consecutiveFailuresCount = 0,
            lastActivityTimestamp = currentTimeMs
        )

        return evaluator.evaluateNextDifficulty(profile, policy, currentTimeMs)
    }
}
