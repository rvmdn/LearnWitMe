package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.RetentionScore
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.StabilityScore
import kotlin.math.exp
import kotlin.math.max

/**
 * Responsibility: Pure, stateless domain service that implements spaced-repetition memory decay
 * algorithms (SuperMemo-2 / FSRS formulation) to calculate retention, stability, and next review dates.
 */
class SpacedRepetitionEngine {

    /**
     * Calculates the next ReviewSchedule based on a review outcome quality rating (0.0 to 1.0) and timestamp.
     */
    fun calculateNextSchedule(
        currentSchedule: ReviewSchedule,
        qualityScore: Double,
        clockTimestamp: Long
    ): ReviewSchedule {
        require(qualityScore in 0.0..1.0) { "Quality score must be bounded between 0.0 and 1.0" }
        require(clockTimestamp > 0) { "Clock timestamp must be positive" }

        val newRepetitions = currentSchedule.repetitions + 1
        val newEaseFactor = max(1.3, currentSchedule.easeFactor + (0.1 - (1.0 - qualityScore) * 0.2))

        val newIntervalDays = when (newRepetitions) {
            1 -> 1
            2 -> 6
            else -> max(1, (currentSchedule.intervalDays * newEaseFactor).toInt())
        }

        val nextTimestamp = clockTimestamp + (newIntervalDays * 24 * 3600 * 1000L)

        return currentSchedule.copy(
            intervalDays = newIntervalDays,
            nextReviewTimestamp = nextTimestamp,
            easeFactor = newEaseFactor,
            repetitions = newRepetitions
        )
    }

    /**
     * Calculates memory retention probability: R = exp(-elapsedDays / stabilityDays).
     */
    fun calculateRetention(stabilityDays: Double, elapsedDays: Double): RetentionScore {
        if (stabilityDays <= 0.0) return RetentionScore(0.0)
        if (elapsedDays <= 0.0) return RetentionScore(1.0)

        val retentionValue = exp(-elapsedDays / stabilityDays).coerceIn(0.0, 1.0)
        return RetentionScore(retentionValue)
    }

    /**
     * Calculates updated memory stability duration in days.
     */
    fun calculateStability(currentStabilityDays: Double, qualityScore: Double): StabilityScore {
        val multiplier = 1.0 + (qualityScore * 1.5)
        val newStability = max(1.0, currentStabilityDays * multiplier)
        return StabilityScore(newStability)
    }
}
