package com.learnwitme.core.application.service

import com.learnwitme.core.domain.adaptive.DifficultyRecommendation
import com.learnwitme.core.domain.adaptive.LearningRecommendation
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.insight.LearningInsight

/**
 * Single Responsibility: Application service that enriches deterministic domain engine outputs
 * (insights, recommendations, difficulty changes) with friendly AI explanation narratives.
 * Rule: Does NOT alter underlying domain decisions or states.
 */
class AIExplanationService(
    private val aiManager: AIManager
) {
    suspend fun enhanceInsight(insight: LearningInsight): LearningInsight {
        val prompt = "Enhance the following learning insight title '${insight.title}' and trend '${insight.trend.name}' into an encouraging learner narrative."
        val response = aiManager.generateExplanation(AIRequest(prompt = prompt, context = insight.description))

        return insight.copy(
            description = "${insight.description}\n\nAI Insight Note:\n${response.content}"
        )
    }

    suspend fun explainRecommendation(recommendation: LearningRecommendation): String {
        val prompt = "Explain why studying ${recommendation.card.content.title} is recommended right now."
        val response = aiManager.generateExplanation(
            AIRequest(prompt = prompt, context = "Reason: ${recommendation.reason.name}, Priority: ${recommendation.priority.name}")
        )
        return response.content
    }

    suspend fun explainDifficultyChange(difficultyRecommendation: DifficultyRecommendation): String {
        val prompt = "Provide encouraging learner guidance for difficulty transition to ${difficultyRecommendation.targetLevel.name}."
        val response = aiManager.generateExplanation(
            AIRequest(prompt = prompt, context = "Reason: ${difficultyRecommendation.reason.name}")
        )
        return response.content
    }
}
