package com.learnwitme.core.application.service

import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Single Responsibility: Application service that expands KnowledgeCards and topics into rich,
 * structured micro-lessons and real-world code examples via AI.
 */
class AILessonService(
    private val aiManager: AIManager
) {
    suspend fun expandCard(card: KnowledgeCard): String {
        val prompt = "Create an expanded deep-dive lesson for technical card '${card.content.title}'."
        val response = aiManager.generateLesson(
            AIRequest(prompt = prompt, context = card.content.summary)
        )
        return response.content
    }

    suspend fun generateMicroLesson(topic: String): String {
        val prompt = "Generate a 3-minute structured micro-lesson for topic '$topic'."
        val response = aiManager.generateLesson(AIRequest(prompt = prompt))
        return response.content
    }

    suspend fun produceLearningExamples(card: KnowledgeCard): String {
        val prompt = "Produce 2 real-world architectural code examples demonstrating '${card.content.title}'."
        val response = aiManager.generateExplanation(
            AIRequest(prompt = prompt, context = "Example baseline: ${card.content.realWorldExample}")
        )
        return response.content
    }
}
