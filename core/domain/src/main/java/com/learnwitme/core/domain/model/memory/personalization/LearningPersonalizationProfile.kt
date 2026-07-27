package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.PatternDifficultyLevel
import com.learnwitme.core.domain.model.memory.StudyTimeWindow

// =====================================================================
// Learning Memory — LearningPersonalizationProfile
// Sprint 9 · Stage 5 · Personalization Context
//
// Responsibility: Represents an immutable, high-level summary of the
// learner's cognitive preferences and behavioral habits.
//
// Extracted from LearningMemory without exposing the raw aggregate state.
// Consumed by AI prompting, content recommendation, and UI presentation.
// =====================================================================

/**
 * Responsibility: Lightweight projection of a learner's pedagogical preferences
 * and study patterns for use in personalization engines and AI prompt synthesis.
 *
 * @param primaryStyle                  Primary cognitive learning style (e.g., VISUAL, PRACTICAL).
 * @param secondaryStyle                Optional secondary cognitive style, if detected.
 * @param preferenceConfidence          Confidence level [0.0, 1.0] of style detection.
 * @param preferenceObservationCount    Total session observations contributing to style.
 * @param bestStudyWindow               Time-of-day window when learner is most active.
 * @param preferredDifficulty           Inferred comfortable content difficulty level.
 * @param averageSessionDurationMinutes Average duration of learning sessions in minutes.
 * @param completionRate                Proportion of started sessions completed [0.0, 1.0].
 * @param totalSessionsAnalyzed         Total number of historical sessions contributing to pattern.
 */
data class LearningPersonalizationProfile(
    val primaryStyle: LearningStyleType,
    val secondaryStyle: LearningStyleType?,
    val preferenceConfidence: Double,
    val preferenceObservationCount: Int,
    val bestStudyWindow: StudyTimeWindow,
    val preferredDifficulty: PatternDifficultyLevel,
    val averageSessionDurationMinutes: Double,
    val completionRate: Double,
    val totalSessionsAnalyzed: Int
) : ValueObject {

    init {
        require(preferenceConfidence in 0.0..1.0) {
            "preferenceConfidence must be in [0.0, 1.0] (got $preferenceConfidence)"
        }
        require(preferenceObservationCount >= 0) {
            "preferenceObservationCount cannot be negative (got $preferenceObservationCount)"
        }
        require(averageSessionDurationMinutes >= 0.0) {
            "averageSessionDurationMinutes cannot be negative (got $averageSessionDurationMinutes)"
        }
        require(completionRate in 0.0..1.0) {
            "completionRate must be in [0.0, 1.0] (got $completionRate)"
        }
        require(totalSessionsAnalyzed >= 0) {
            "totalSessionsAnalyzed cannot be negative (got $totalSessionsAnalyzed)"
        }
    }

    /** True if preference has been determined with high confidence (≥ 0.60). */
    val isHighConfidencePreference: Boolean
        get() = primaryStyle != LearningStyleType.UNDETERMINED && preferenceConfidence >= 0.60

    /** True if session completion habit is consistent (≥ 0.80). */
    val isConsistentFinisher: Boolean
        get() = completionRate >= 0.80

    companion object {

        /**
         * Creates a [LearningPersonalizationProfile] directly from [LearningPreference]
         * and [LearningPattern].
         */
        fun from(
            preference: LearningPreference,
            pattern: LearningPattern
        ): LearningPersonalizationProfile {
            return LearningPersonalizationProfile(
                primaryStyle = preference.primaryStyle,
                secondaryStyle = preference.secondaryStyle,
                preferenceConfidence = preference.confidenceScore,
                preferenceObservationCount = preference.observationCount,
                bestStudyWindow = pattern.bestStudyWindow,
                preferredDifficulty = pattern.preferredDifficulty,
                averageSessionDurationMinutes = pattern.averageSessionDurationMs / 60_000.0,
                completionRate = pattern.completionRate,
                totalSessionsAnalyzed = pattern.sessionCount
            )
        }

        /**
         * Returns an initial/default profile when no historical data exists.
         */
        fun default(): LearningPersonalizationProfile {
            return LearningPersonalizationProfile(
                primaryStyle = LearningStyleType.UNDETERMINED,
                secondaryStyle = null,
                preferenceConfidence = 0.0,
                preferenceObservationCount = 0,
                bestStudyWindow = StudyTimeWindow.UNKNOWN,
                preferredDifficulty = PatternDifficultyLevel.UNKNOWN,
                averageSessionDurationMinutes = 0.0,
                completionRate = 0.0,
                totalSessionsAnalyzed = 0
            )
        }
    }
}
