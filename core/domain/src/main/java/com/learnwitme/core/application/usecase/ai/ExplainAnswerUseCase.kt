package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.application.ai.personalization.LearningPromptContextBuilder
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.AIResultMapper
import com.learnwitme.core.application.ai.result.GeneratedExplanation
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Single Responsibility: Application use case for generating personalized pedagogical explanations.
 */
class ExplainAnswerUseCase(
    private val aiManager: AIManager,
    private val mapper: AIResultMapper = AIResultMapper(),
    private val promptBuilder: LearningPromptContextBuilder = LearningPromptContextBuilder()
) {
    suspend operator fun invoke(
        question: String,
        selectedAnswer: String,
        expectedAnswer: String,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.EXPLANATION_ONLY
    ): GeneratedExplanation {
        val requestPrompt = buildString {
            append("Explain the reasoning for the question: '$question'.\n")
            append("Selected Answer: '$selectedAnswer'\n")
            append("Expected Answer: '$expectedAnswer'")
        }
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.generateExplanation(aiRequest)
        return mapper.mapToGeneratedExplanation(response, fallbackTitle = "Explanation for: $question")
    }
}
