package com.learnwitme.core.application.ai.personalization

import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Pure, token-efficient, deterministic builder transforming a learner's [LearningPersonalizationContext]
 * into a structured, privacy-compliant [PersonalizedPrompt].
 */
class LearningPromptContextBuilder {

    fun buildPrompt(
        requestPrompt: String,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.FULL
    ): PersonalizedPrompt {
        val metadata = mutableMapOf<String, String>()
        metadata["policyPreset"] = policy.presetName

        val systemPrompt = buildString {
            append("You are an expert personalized AI tutor for the LearnWitMe learning platform.\n")
            append("Provide clear, encouraging, structured, and pedagogical responses tailored to the learner's cognitive profile.")
        }

        val userPrompt = buildString {
            if (personalizationContext != null) {
                metadata["userId"] = personalizationContext.userId.value
                metadata["memoryId"] = personalizationContext.memoryId.value

                appendLine("=== LEARNER PERSONALIZATION CONTEXT ===")

                if (policy.includedSections.contains(PersonalizedPromptSection.LEARNING_STYLE)) {
                    appendLine("Learning Style: ${personalizationContext.profile.primaryStyle}" +
                            if (personalizationContext.profile.secondaryStyle != null) " (Secondary: ${personalizationContext.profile.secondaryStyle})" else "")
                    appendLine("Difficulty Preference: ${personalizationContext.profile.preferredDifficulty}")
                }

                if (policy.includedSections.contains(PersonalizedPromptSection.STUDY_PATTERN)) {
                    appendLine("Study Window: ${personalizationContext.profile.bestStudyWindow}")
                    appendLine("Avg Session Duration: ${"%.1f".format(personalizationContext.profile.averageSessionDurationMinutes)} mins")
                }

                if (policy.includedSections.contains(PersonalizedPromptSection.STRENGTHS) && personalizationContext.hasStrengths) {
                    val strengthList = personalizationContext.strengths.joinToString { "${it.topicName} (${it.topicCode})" }
                    appendLine("Learner Strengths: $strengthList")
                }

                if (policy.includedSections.contains(PersonalizedPromptSection.WEAKNESSES) && personalizationContext.hasWeaknesses) {
                    val weaknessList = personalizationContext.weaknesses.joinToString { "${it.topicName} [${it.severity}] (${it.topicCode})" }
                    appendLine("Learner Weaknesses: $weaknessList")
                }

                if (policy.includedSections.contains(PersonalizedPromptSection.GOALS) && personalizationContext.hasActiveGoals) {
                    appendLine("Active Goals: ${personalizationContext.activeGoals.joinToString()}")
                }

                if (policy.allowInternalRetentionMetrics && personalizationContext.totalCardsTracked > 0) {
                    appendLine("Retention Rate: ${"%.2f".format(personalizationContext.overallRetentionAverage)}")
                }

                appendLine("=======================================")
                appendLine()
            }

            if (policy.includedSections.contains(PersonalizedPromptSection.CURRENT_REQUEST)) {
                appendLine("=== CURRENT REQUEST ===")
                append(requestPrompt)
            }
        }.trim()

        val estimatedTokens = (systemPrompt.length + userPrompt.length) / 4

        return PersonalizedPrompt(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            metadata = metadata,
            estimatedTokens = estimatedTokens
        )
    }
}
