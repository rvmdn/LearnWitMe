package com.learnwitme.core.application.ai.personalization

import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationPolicy

/**
 * High-level policy preset dictating section inclusion, privacy boundaries, and token budget constraints.
 */
data class PersonalizedPromptPolicy(
    val presetName: String = "FULL",
    val domainPolicy: LearningPersonalizationPolicy = LearningPersonalizationPolicy.FULL,
    val includedSections: Set<PersonalizedPromptSection> = PersonalizedPromptSection.values().toSet(),
    val maxTokenBudget: Int = 1024,
    val allowInternalRetentionMetrics: Boolean = false
) {
    companion object {
        val FULL = PersonalizedPromptPolicy(
            presetName = "FULL",
            domainPolicy = LearningPersonalizationPolicy.FULL,
            includedSections = PersonalizedPromptSection.values().toSet(),
            maxTokenBudget = 1024,
            allowInternalRetentionMetrics = false
        )

        val CONCISE = PersonalizedPromptPolicy(
            presetName = "CONCISE",
            domainPolicy = LearningPersonalizationPolicy.CONCISE_AI,
            includedSections = setOf(
                PersonalizedPromptSection.LEARNING_STYLE,
                PersonalizedPromptSection.WEAKNESSES,
                PersonalizedPromptSection.CURRENT_REQUEST
            ),
            maxTokenBudget = 512,
            allowInternalRetentionMetrics = false
        )

        val QUIZ_ONLY = PersonalizedPromptPolicy(
            presetName = "QUIZ_ONLY",
            domainPolicy = LearningPersonalizationPolicy.REMEDIATION_ONLY,
            includedSections = setOf(
                PersonalizedPromptSection.WEAKNESSES,
                PersonalizedPromptSection.CURRENT_REQUEST
            ),
            maxTokenBudget = 512,
            allowInternalRetentionMetrics = false
        )

        val LESSON_ONLY = PersonalizedPromptPolicy(
            presetName = "LESSON_ONLY",
            domainPolicy = LearningPersonalizationPolicy.CONCISE_AI,
            includedSections = setOf(
                PersonalizedPromptSection.LEARNING_STYLE,
                PersonalizedPromptSection.STRENGTHS,
                PersonalizedPromptSection.GOALS,
                PersonalizedPromptSection.CURRENT_REQUEST
            ),
            maxTokenBudget = 768,
            allowInternalRetentionMetrics = false
        )

        val EXPLANATION_ONLY = PersonalizedPromptPolicy(
            presetName = "EXPLANATION_ONLY",
            domainPolicy = LearningPersonalizationPolicy.CONCISE_AI,
            includedSections = setOf(
                PersonalizedPromptSection.LEARNING_STYLE,
                PersonalizedPromptSection.WEAKNESSES,
                PersonalizedPromptSection.CURRENT_REQUEST
            ),
            maxTokenBudget = 512,
            allowInternalRetentionMetrics = false
        )

        val STRICT_PRIVACY = PersonalizedPromptPolicy(
            presetName = "STRICT_PRIVACY",
            domainPolicy = LearningPersonalizationPolicy.STRICT_PRIVACY,
            includedSections = setOf(
                PersonalizedPromptSection.LEARNING_STYLE,
                PersonalizedPromptSection.CURRENT_REQUEST
            ),
            maxTokenBudget = 256,
            allowInternalRetentionMetrics = false
        )
    }
}
