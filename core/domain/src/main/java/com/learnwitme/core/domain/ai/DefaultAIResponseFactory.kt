package com.learnwitme.core.domain.ai

/**
 * Responsibility: Owns deterministic response templates for explanations, micro-lessons,
 * quiz generation, and content simplification.
 */
class DefaultAIResponseFactory {

    internal fun extractCleanTopic(rawPrompt: String): String {
        if (rawPrompt.isBlank()) return "Core Concept"

        val promptBody = if (rawPrompt.contains("=== CURRENT REQUEST ===")) {
            rawPrompt.substringAfter("=== CURRENT REQUEST ===").trim()
        } else {
            rawPrompt.trim()
        }

        if (promptBody.isBlank()) return "Core Concept"

        val questionMatch = Regex("(?:question|topic|expanding on)\\s*[:=]?\\s*['\"]([^'\"]+)['\"]", RegexOption.IGNORE_CASE)
            .find(promptBody)
        val extractedTopic = if (questionMatch != null) {
            val extracted = questionMatch.groupValues[1].trim()
            val innerQuoted = Regex("['\"]([^'\"]+)['\"]").find(extracted)
            if (innerQuoted != null) {
                innerQuoted.groupValues[1].trim()
            } else {
                val ofMatch = Regex("of\\s+([A-Za-z0-9 &+\\-]+)$", RegexOption.IGNORE_CASE).find(extracted)
                if (ofMatch != null) ofMatch.groupValues[1].trim() else extracted
            }
        } else {
            val firstQuoted = Regex("['\"]([^'\"]+)['\"]").find(promptBody)
            if (firstQuoted != null) {
                firstQuoted.groupValues[1].trim()
            } else {
                promptBody.lines().firstOrNull { it.isNotBlank() }?.trim() ?: "Core Concept"
            }
        }

        val cleaned = extractedTopic
            .replace(Regex("^(?:system|instruction|prompt):\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[#*`\"\\\\]"), "")
            .trim()

        return if (cleaned.isBlank()) "Core Concept" else cleaned.take(80)
    }

    fun createExplanationResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val topic = extractCleanTopic(request.prompt)
        val content = """
            |### Explanation for: $topic
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
        val topic = extractCleanTopic(request.prompt)
        val content = """
            |# Micro-Lesson: $topic
            |
            |## Objective
            |Master the key concepts behind $topic.
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
        val topic = extractCleanTopic(request.prompt)
        val content = """
            |{
            |  "question": "What is the primary architectural goal of $topic?",
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
        val topic = extractCleanTopic(request.prompt)
        val content = """
            |**Simplified**: $topic
            |
            |In simple terms: $topic helps you learn step-by-step with clear explanations and easy rules.
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
