package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.application.ai.personalization.LearningPromptContextBuilder
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.AIResultMapper
import com.learnwitme.core.application.ai.result.SimplifiedContent
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Single Responsibility: Application use case for simplifying complex technical content.
 */
class SimplifyTopicUseCase(
    private val aiManager: AIManager,
    private val mapper: AIResultMapper = AIResultMapper(),
    private val promptBuilder: LearningPromptContextBuilder = LearningPromptContextBuilder()
) {
    suspend operator fun invoke(
        technicalContent: String,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.CONCISE
    ): SimplifiedContent {
        val requestPrompt = "Simplify the following technical content for a beginner learner:\n$technicalContent"
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.simplifyContent(aiRequest)
        return mapper.mapToSimplifiedContent(response, technicalContent)
    }
}
