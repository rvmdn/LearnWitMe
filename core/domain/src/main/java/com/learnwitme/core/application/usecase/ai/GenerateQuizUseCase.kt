package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.application.ai.personalization.LearningPromptContextBuilder
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.AIResultMapper
import com.learnwitme.core.application.ai.result.GeneratedQuiz
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Single Responsibility: Application use case for generating personalized interactive evaluation quizzes.
 */
class GenerateQuizUseCase(
    private val aiManager: AIManager,
    private val mapper: AIResultMapper = AIResultMapper(),
    private val promptBuilder: LearningPromptContextBuilder = LearningPromptContextBuilder()
) {
    suspend operator fun invoke(
        card: KnowledgeCard,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.QUIZ_ONLY
    ): GeneratedQuiz {
        val requestPrompt = "Generate a multiple-choice evaluation quiz for '${card.content.title}'. Summary: ${card.content.summary}"
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.generateQuiz(aiRequest)
        return mapper.mapToGeneratedQuiz(response, fallbackTitle = card.content.title)
    }

    suspend operator fun invoke(
        topic: String,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.QUIZ_ONLY
    ): GeneratedQuiz {
        val requestPrompt = "Generate a multiple-choice evaluation quiz for topic: '$topic'."
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.generateQuiz(aiRequest)
        return mapper.mapToGeneratedQuiz(response, fallbackTitle = topic)
    }
}
