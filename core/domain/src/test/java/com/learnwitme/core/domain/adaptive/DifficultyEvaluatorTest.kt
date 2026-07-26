package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Test

class DifficultyEvaluatorTest {

    private val evaluator = DifficultyEvaluator()
    private val userId = UserId("usr_adaptive_1")

    @Test
    fun testPromotionOnHighAccuracy() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.BEGINNER,
            totalSessionsCompleted = 5,
            overallAccuracy = 0.95,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.EASY, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.HIGH_ACCURACY, result.reason)
    }

    @Test
    fun testPromotionOnHighMastery() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.EASY,
            totalSessionsCompleted = 10,
            overallAccuracy = 0.85,
            masteryPercentage = 0.96,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.MEDIUM, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.MASTERED_TOPIC, result.reason)
    }

    @Test
    fun testDemotionOnLowAccuracy() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.MEDIUM,
            totalSessionsCompleted = 5,
            overallAccuracy = 0.45,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.EASY, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.LOW_ACCURACY, result.reason)
    }

    @Test
    fun testDemotionOnRepeatedFailures() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.HARD,
            totalSessionsCompleted = 5,
            consecutiveFailuresCount = 4,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.MEDIUM, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.REPEATED_FAILURES, result.reason)
    }

    @Test
    fun testLongInactivityMaintainsLevel() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.MEDIUM,
            totalSessionsCompleted = 5,
            overallAccuracy = 0.95,
            lastActivityTimestamp = 1000L
        )

        val eightDaysMs = 8 * 24 * 60 * 60 * 1000L
        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 1000L + eightDaysMs)

        assertEquals(AdaptiveDifficultyLevel.MEDIUM, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.LONG_INACTIVITY, result.reason)
    }

    @Test
    fun testBeginnerCannotDemoteBelowBeginner() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.BEGINNER,
            totalSessionsCompleted = 5,
            overallAccuracy = 0.30,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.BEGINNER, result.targetLevel)
    }

    @Test
    fun testExpertCannotPromoteAboveExpert() {
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.EXPERT,
            totalSessionsCompleted = 50,
            overallAccuracy = 0.98,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.EXPERT, result.targetLevel)
    }

    @Test
    fun testCustomPolicyThresholds() {
        val customPolicy = DifficultyPolicy(highAccuracyThreshold = 0.80)
        val profile = DifficultyProfile(
            userId = userId,
            currentLevel = AdaptiveDifficultyLevel.BEGINNER,
            totalSessionsCompleted = 5,
            overallAccuracy = 0.85,
            lastActivityTimestamp = 1000L
        )

        val result = evaluator.evaluateNextDifficulty(profile, customPolicy, currentTimeMs = 2000L)
        assertEquals(AdaptiveDifficultyLevel.EASY, result.targetLevel)
        assertEquals(DifficultyAdjustmentReason.HIGH_ACCURACY, result.reason)
    }

    @Test
    fun testDeterministicResult() {
        val profile = DifficultyProfile(userId = userId, currentLevel = AdaptiveDifficultyLevel.EASY, overallAccuracy = 0.92, totalSessionsCompleted = 5)
        val res1 = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), 2000L)
        val res2 = evaluator.evaluateNextDifficulty(profile, DifficultyPolicy(), 2000L)

        assertEquals(res1, res2)
    }
}
