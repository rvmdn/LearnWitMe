package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.ValueObject
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningWeakness

// =====================================================================
// Learning Memory — LearningPersonalizationContext
// Sprint 9 · Stage 5 · Personalization Context
//
// Responsibility: Represents an immutable, privacy-filtered, token-efficient
// read-model projection of a learner's memory state.
//
// LearningMemory aggregate remains strictly INTERNAL to the memory domain.
// AI infrastructure, recommendation engines, and presentation layers consume
// ONLY this LearningPersonalizationContext.
// =====================================================================

/**
 * Responsibility: Immutable read model projecting personalization details
 * from a learner's memory state according to a [LearningPersonalizationPolicy].
 *
 * @param userId                 ID of the learner.
 * @param memoryId               ID of the source LearningMemory aggregate.
 * @param profile                High-level cognitive preference and study pattern profile.
 * @param strengths              Filtered list of topic strengths.
 * @param weaknesses             Filtered list of topic weaknesses.
 * @param behaviors              Filtered list of observable learning behaviors.
 * @param overallRetentionAverage Overall retention average [0.0, 1.0]. 0.0 if not included.
 * @param totalCardsTracked      Total number of cards tracked. 0 if not included.
 * @param activeGoals            List of active target goal topic codes or descriptions.
 * @param generatedEpochMs       Epoch timestamp (ms) when this context was generated.
 */
data class LearningPersonalizationContext(
    val userId: UserId,
    val memoryId: LearningMemoryId,
    val profile: LearningPersonalizationProfile,
    val strengths: List<LearningStrength>,
    val weaknesses: List<LearningWeakness>,
    val behaviors: List<LearningBehavior>,
    val overallRetentionAverage: Double,
    val totalCardsTracked: Int,
    val activeGoals: List<String>,
    val generatedEpochMs: Long
) : ValueObject {

    init {
        require(overallRetentionAverage in 0.0..1.0) {
            "overallRetentionAverage must be in [0.0, 1.0] (got $overallRetentionAverage)"
        }
        require(totalCardsTracked >= 0) {
            "totalCardsTracked cannot be negative (got $totalCardsTracked)"
        }
        require(generatedEpochMs > 0) {
            "generatedEpochMs must be positive (got $generatedEpochMs)"
        }
    }

    /** True if the learner has any active strengths in this context. */
    val hasStrengths: Boolean get() = strengths.isNotEmpty()

    /** True if the learner has any active weaknesses in this context. */
    val hasWeaknesses: Boolean get() = weaknesses.isNotEmpty()

    /** True if any weakness in this context is severe. */
    val hasSevereWeakness: Boolean get() = weaknesses.any { it.isCritical }

    /** True if the learner has any active goals. */
    val hasActiveGoals: Boolean get() = activeGoals.isNotEmpty()

    /**
     * Formats this personalization context into a structured, concise string
     * suitable for direct injection into AI system prompts or LLM context blocks.
     */
    fun toPromptContext(): String = buildString {
        appendLine("=== LEARNER PERSONALIZATION CONTEXT ===")
        appendLine("Learning Style : ${profile.primaryStyle}" +
                if (profile.secondaryStyle != null) " (Secondary: ${profile.secondaryStyle})" else "")
        appendLine("Study Window   : ${profile.bestStudyWindow}")
        appendLine("Difficulty     : ${profile.preferredDifficulty}")
        appendLine("Avg Duration   : ${"%.1f".format(profile.averageSessionDurationMinutes)} mins")
        appendLine("Completion Rate: ${"%.0f".format(profile.completionRate * 100)}%")

        if (strengths.isNotEmpty()) {
            appendLine("Strengths      : ${strengths.joinToString { "${it.topicName} (${it.topicCode})" }}")
        }
        if (weaknesses.isNotEmpty()) {
            appendLine("Weaknesses     : ${weaknesses.joinToString { "${it.topicName} [${it.severity}] (${it.topicCode})" }}")
        }
        if (behaviors.isNotEmpty()) {
            appendLine("Behaviors      : ${behaviors.joinToString { it.type.name }}")
        }
        if (activeGoals.isNotEmpty()) {
            appendLine("Active Goals   : ${activeGoals.joinToString()}")
        }
        if (totalCardsTracked > 0) {
            appendLine("Retention Avg  : ${"%.2f".format(overallRetentionAverage)} across $totalCardsTracked cards")
        }
        append("=======================================")
    }
}
