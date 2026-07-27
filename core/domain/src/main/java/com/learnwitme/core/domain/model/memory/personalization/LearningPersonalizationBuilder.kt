package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness

// =====================================================================
// Learning Memory — LearningPersonalizationBuilder
// Sprint 9 · Stage 5 · Personalization Context
//
// Responsibility: Maps an internal LearningMemory aggregate into an
// external, read-only LearningPersonalizationContext projection.
//
// Ensures that LearningMemory itself is never directly exposed to AI
// infrastructure, recommendation engines, or presentation layers.
// =====================================================================

/**
 * Responsibility: Deterministic mapping engine that projects a [LearningMemory]
 * aggregate into a [LearningPersonalizationContext] governed by a [LearningPersonalizationPolicy].
 *
 * Fully stateless — no instance fields, no side effects, no repository calls.
 */
object LearningPersonalizationBuilder {

    /**
     * Projects [memory] into a [LearningPersonalizationContext] using the rules in [policy].
     *
     * @param memory       The internal [LearningMemory] aggregate.
     * @param policy       The [LearningPersonalizationPolicy] controlling exposure.
     *                     Defaults to [LearningPersonalizationPolicy.FULL].
     * @param activeGoals  Optional list of active goal topic codes or target descriptions.
     * @param nowEpochMs   Epoch timestamp (ms) for generating the context.
     * @return An immutable [LearningPersonalizationContext].
     */
    fun build(
        memory: LearningMemory,
        policy: LearningPersonalizationPolicy = LearningPersonalizationPolicy.FULL,
        activeGoals: List<String> = emptyList(),
        nowEpochMs: Long
    ): LearningPersonalizationContext {

        // 1. Build profile
        val profile = LearningPersonalizationProfile.from(
            preference = memory.preference,
            pattern = memory.pattern
        )

        // 2. Filter strengths
        val filteredStrengths: List<LearningStrength> = if (policy.includeStrengths) {
            val sorted = memory.strengths.sortedByDescending { it.masteryRate }
            if (policy.maxStrengthsToInclude > 0) {
                sorted.take(policy.maxStrengthsToInclude)
            } else {
                sorted
            }
        } else {
            emptyList()
        }

        // 3. Filter weaknesses
        val filteredWeaknesses: List<LearningWeakness> = if (policy.includeWeaknesses) {
            val qualifying = memory.weaknesses.filter { weakness ->
                weakness.severity.ordinal >= policy.minWeaknessSeverity.ordinal
            }.sortedWith(
                compareByDescending<LearningWeakness> { it.severity.ordinal }
                    .thenBy { it.averageRetention }
            )

            if (policy.maxWeaknessesToInclude > 0) {
                qualifying.take(policy.maxWeaknessesToInclude)
            } else {
                qualifying
            }
        } else {
            emptyList()
        }

        // 4. Filter behaviors
        val filteredBehaviors: List<LearningBehavior> = if (policy.includeBehaviors) {
            val qualifying = memory.behaviors.filter { behavior ->
                behavior.occurrenceRate >= policy.minBehaviorOccurrenceRate
            }.sortedByDescending { it.lastObservedEpochMs }

            if (policy.maxBehaviorsToInclude > 0) {
                qualifying.take(policy.maxBehaviorsToInclude)
            } else {
                qualifying
            }
        } else {
            emptyList()
        }

        // 5. Compute metrics if policy allows
        val overallRetention = if (policy.includeOverallMetrics) {
            memory.computeOverallRetention()
        } else {
            0.0
        }

        val cardCount = if (policy.includeOverallMetrics) {
            memory.records.size
        } else {
            0
        }

        return LearningPersonalizationContext(
            userId = memory.userId,
            memoryId = memory.id,
            profile = profile,
            strengths = filteredStrengths,
            weaknesses = filteredWeaknesses,
            behaviors = filteredBehaviors,
            overallRetentionAverage = overallRetention,
            totalCardsTracked = cardCount,
            activeGoals = activeGoals,
            generatedEpochMs = nowEpochMs
        )
    }
}
