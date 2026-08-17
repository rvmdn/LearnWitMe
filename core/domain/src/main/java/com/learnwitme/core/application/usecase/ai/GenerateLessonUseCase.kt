package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.application.ai.personalization.LearningPromptContextBuilder
import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.AIResultMapper
import com.learnwitme.core.application.ai.result.GeneratedLesson
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Single Responsibility: Application use case for generating a personalized learning lesson
 * tailored to a learner's cognitive profile and topic.
 */
class GenerateLessonUseCase(
    private val aiManager: AIManager,
    private val mapper: AIResultMapper = AIResultMapper(),
    private val promptBuilder: LearningPromptContextBuilder = LearningPromptContextBuilder()
) {
    suspend operator fun invoke(
        topic: String,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.LESSON_ONLY
    ): GeneratedLesson {
        val requestPrompt = "Generate a comprehensive, structured lesson covering topic: '$topic'."
        val personalizedPrompt = promptBuilder.buildPrompt(requestPrompt, personalizationContext, policy)

        val aiRequest = AIRequest(
            prompt = personalizedPrompt.userPrompt,
            systemInstruction = personalizedPrompt.systemPrompt
        )
        val response = aiManager.generateLesson(aiRequest)
        return mapper.mapToGeneratedLesson(response, fallbackTitle = topic)
    }
}
