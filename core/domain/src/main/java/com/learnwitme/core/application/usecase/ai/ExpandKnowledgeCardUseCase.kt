package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.application.ai.personalization.LearningPromptContextBuilder
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.AIResultMapper
import com.learnwitme.core.application.ai.result.ExpandedKnowledgeCard
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Single Responsibility: Application use case for expanding a KnowledgeCard into a personalized deep-dive lesson.
 */
class ExpandKnowledgeCardUseCase(
    private val aiManager: AIManager,
    private val mapper: AIResultMapper = AIResultMapper(),
    private val promptBuilder: LearningPromptContextBuilder = LearningPromptContextBuilder()
) {
    suspend operator fun invoke(
        card: KnowledgeCard,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.LESSON_ONLY
    ): ExpandedKnowledgeCard {
        val requestPrompt = "Provide a deep-dive, detailed explanation expanding on '${card.content.title}'. Summary: ${card.content.summary}"
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.generateLesson(aiRequest)
        return mapper.mapToExpandedCard(response, card)
    }
}
