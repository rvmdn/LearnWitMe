package com.learnwitme.core.application.service

import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Single Responsibility: Application service that synthesizes interactive quizzes and detailed
 * answer feedback from existing KnowledgeCards using AI.
 */
class AIQuizService(
    private val aiManager: AIManager
) {
    suspend fun generateQuizFromCard(card: KnowledgeCard): String {
        val prompt = "Generate a multiple-choice evaluation quiz for '${card.content.title}'."
        val response = aiManager.generateQuiz(
            AIRequest(prompt = prompt, context = card.content.summary)
        )
        return response.content
    }

    suspend fun explainAnswer(question: String, selectedOption: String, isCorrect: Boolean): String {
        val prompt = "Provide educational feedback for question '$question' where user selected '$selectedOption'."
        val response = aiManager.generateExplanation(
            AIRequest(prompt = prompt, context = "Is Answer Correct: $isCorrect")
        )
        return response.content
    }
}
