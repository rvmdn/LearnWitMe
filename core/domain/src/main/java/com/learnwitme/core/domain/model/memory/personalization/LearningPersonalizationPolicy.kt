package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.WeaknessSeverity

// =====================================================================
// Learning Memory — LearningPersonalizationPolicy
// Sprint 9 · Stage 5 · Personalization Context
//
// Responsibility: Encapsulates filtering, privacy, and scope rules
// for generating a LearningPersonalizationContext.
//
// Ensures callers (such as AI prompts or UI widgets) receive only the
// subset of personalization data relevant to their operational context.
// =====================================================================

/**
 * Responsibility: Policy configuration object governing how
 * [LearningPersonalizationBuilder] projects [com.learnwitme.core.domain.model.LearningMemory]
 * into a [LearningPersonalizationContext].
 *
 * @param includeStrengths            Whether to include strengths in the context. Default: true.
 * @param maxStrengthsToInclude       Maximum number of top strengths to include. 0 means unlimited. Default: 5.
 * @param includeWeaknesses           Whether to include weaknesses in the context. Default: true.
 * @param minWeaknessSeverity         Minimum weakness severity required to be included. Default: MILD.
 * @param maxWeaknessesToInclude      Maximum number of weaknesses to include. 0 means unlimited. Default: 5.
 * @param includeBehaviors            Whether to include observed behaviors. Default: true.
 * @param minBehaviorOccurrenceRate   Minimum occurrence rate [0.0, 1.0] for behaviors to be included. Default: 0.30.
 * @param maxBehaviorsToInclude       Maximum number of behaviors to include. 0 means unlimited. Default: 5.
 * @param includeOverallMetrics       Whether to include retention average and tracked card count. Default: true.
 */
data class LearningPersonalizationPolicy(
    val includeStrengths: Boolean = true,
    val maxStrengthsToInclude: Int = 5,
    val includeWeaknesses: Boolean = true,
    val minWeaknessSeverity: WeaknessSeverity = WeaknessSeverity.MILD,
    val maxWeaknessesToInclude: Int = 5,
    val includeBehaviors: Boolean = true,
    val minBehaviorOccurrenceRate: Double = 0.30,
    val maxBehaviorsToInclude: Int = 5,
    val includeOverallMetrics: Boolean = true
) : ValueObject {

    init {
        require(maxStrengthsToInclude >= 0) {
            "maxStrengthsToInclude cannot be negative (got $maxStrengthsToInclude)"
        }
        require(maxWeaknessesToInclude >= 0) {
            "maxWeaknessesToInclude cannot be negative (got $maxWeaknessesToInclude)"
        }
        require(minBehaviorOccurrenceRate in 0.0..1.0) {
            "minBehaviorOccurrenceRate must be in [0.0, 1.0] (got $minBehaviorOccurrenceRate)"
        }
        require(maxBehaviorsToInclude >= 0) {
            "maxBehaviorsToInclude cannot be negative (got $maxBehaviorsToInclude)"
        }
    }

    companion object {

        /**
         * Full context policy — includes all available dimensions up to default limits.
         */
        val FULL: LearningPersonalizationPolicy = LearningPersonalizationPolicy()

        /**
         * Optimized policy for AI LLM prompts — keeps context tight and token-efficient.
         * Limits strengths and weaknesses to top 3 and filters for MODERATE+ weaknesses.
         */
        val CONCISE_AI: LearningPersonalizationPolicy = LearningPersonalizationPolicy(
            maxStrengthsToInclude = 3,
            minWeaknessSeverity = WeaknessSeverity.MODERATE,
            maxWeaknessesToInclude = 3,
            minBehaviorOccurrenceRate = 0.40,
            maxBehaviorsToInclude = 3
        )

        /**
         * Remediation-focused policy — emphasizes weaknesses for targeted practice generation.
         */
        val REMEDIATION_ONLY: LearningPersonalizationPolicy = LearningPersonalizationPolicy(
            includeStrengths = false,
            includeWeaknesses = true,
            minWeaknessSeverity = WeaknessSeverity.MILD,
            maxWeaknessesToInclude = 10,
            includeBehaviors = false
        )

        /**
         * Strict privacy / minimal policy — exposes only top-level profile (style & pattern),
         * omitting specific topic codes.
         */
        val STRICT_PRIVACY: LearningPersonalizationPolicy = LearningPersonalizationPolicy(
            includeStrengths = false,
            includeWeaknesses = false,
            includeBehaviors = false,
            includeOverallMetrics = false
        )
    }
}
