package com.learnwitme.core.domain.ai

/**
 * Responsibility: Owns deterministic response templates for explanations, micro-lessons,
 * quiz generation, and content simplification.
 */
class DefaultAIResponseFactory {

    fun createExplanationResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val content = """
            |### Explanation for: ${request.prompt}
            |
            |**Key Concept Overview**:
            |${request.context.ifBlank { "Core fundamental principles and conceptual framework." }}
            |
            |**Deep Dive Summary**:
            |This topic focuses on structured mastery, clear architectural boundaries, and practical usage patterns.
        """.trimMargin()

        return AIResponse(
            content = content,
            tokensUsed = content.length / 4,
            finishReason = "STOP",
            modelId = config.modelId,
            executionTimeMs = config.simulatedExecutionTimeMs
        )
    }

    fun createLessonResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val content = """
            |# Micro-Lesson: ${request.prompt}
            |
            |## Objective
            |Master the key concepts behind ${request.prompt}.
            |
            |## Core Breakdown
            |1. **Foundation**: ${request.context.ifBlank { "Understand the baseline rules." }}
            |2. **Application**: Apply patterns systematically without breaking existing contracts.
            |3. **Best Practices**: Keep implementations small, composable, and testable.
        """.trimMargin()

        return AIResponse(
            content = content,
            tokensUsed = content.length / 4,
            finishReason = "STOP",
            modelId = config.modelId,
            executionTimeMs = config.simulatedExecutionTimeMs
        )
    }

    fun createQuizResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val content = """
            |{
            |  "question": "What is the primary architectural goal of ${request.prompt}?",
            |  "options": [
            |    "Deterministic business logic execution",
            |    "Direct coupling to UI components",
            |    "Random non-repeatable state changes",
            |    "Bypassing repository abstractions"
            |  ],
            |  "correctAnswerIndex": 0,
            |  "explanation": "LearnWitMe mandates deterministic domain engines decoupled from UI and framework layers."
            |}
        """.trimMargin()

        return AIResponse(
            content = content,
            tokensUsed = content.length / 4,
            finishReason = "STOP",
            modelId = config.modelId,
            executionTimeMs = config.simulatedExecutionTimeMs
        )
    }

    fun createSimplifiedContentResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val content = """
            |**Simplified**: ${request.prompt}
            |
            |In simple terms: ${request.prompt} helps you learn step-by-step with clear explanations and easy rules.
        """.trimMargin()

        return AIResponse(
            content = content,
            tokensUsed = content.length / 4,
            finishReason = "STOP",
            modelId = config.modelId,
            executionTimeMs = config.simulatedExecutionTimeMs
        )
    }
}
