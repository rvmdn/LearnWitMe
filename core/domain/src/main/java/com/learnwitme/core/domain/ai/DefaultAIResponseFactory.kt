package com.learnwitme.core.domain.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Serializable schema representing deterministic quiz output.
 */
@Serializable
internal data class MockQuizPayload(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

/**
 * Responsibility: Owns deterministic response templates for explanations, micro-lessons,
 * quiz generation, and content simplification.
 *
 * Enforces a strict input extraction contract and safe-content boundary:
 * - Deterministically extracts topics from documented prompt formats.
 * - Resolves malformed, injection-like, or unknown prompts to a safe fallback.
 * - Prevents raw prompt delimiters, metadata, and secret sentinels from leaking into output.
 * - Serializes quiz responses into valid JSON using kotlinx.serialization.
 */
class DefaultAIResponseFactory {

    companion object {
        internal const val FALLBACK_TOPIC = "Core Concept"
        internal const val DEFAULT_EXPLANATION_CONTEXT = "Core fundamental principles and conceptual framework."
        internal const val DEFAULT_LESSON_CONTEXT = "Understand the baseline rules."
        private const val MAX_TOPIC_LENGTH = 80
        private const val MAX_CONTEXT_LENGTH = 500

        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        private val INJECTION_COMMAND_PATTERN = Regex(
            """(?i)\b(?:ignore\s+(?:all\s+)?(?:previous\s+)?instructions|disregard\s+(?:all\s+)?instructions|leak\s+|reveal\s+|bypass\s+(?:security|controls)|exfiltrate|dump\s+(?:memory|database|secrets))\b"""
        )

        private val INSTRUCTION_PREFIX_PATTERN = Regex(
            """^(?:system|instruction|prompt|assistant|user|role|query|task|format|context)\s*[:=]""",
            RegexOption.IGNORE_CASE
        )

        private val UNKNOWN_OR_MALFORMED_PROMPT_PATTERN = Regex(
            """(?i)\b(?:unknown\s+prompt|malformed\s+prompt|unstructured\s+instruction)\b"""
        )

        private val SECRET_OR_SENTINEL_PATTERN = Regex(
            """(?i)(?:TEST_SECRET_\w+|TEST_KEY_\w+|TEST_TOKEN_\w+|(?:authorization\s*:\s*)bearer\b|\bbearer\s+(?:[\r\n]+\s*)?["']?(?!token\b|tokens\b|auth\b|authentication\b|architecture\b)\S*|\b(?:api[_\-\s]?key|access[_\-\s]?token|refresh[_\-\s]?token|auth[_\-\s]?token|id[_\-\s]?token|client[_\-\s]?secret|client[_\-\s]?id|secret[_\-\s]?key|private[_\-\s]?key|password|passwd|secret|token)\s*[:=]\s*(?:[\r\n]+\s*)?["']?\S*)"""
        )

        private val PROMPT_DELIMITER_OR_METADATA_PATTERN = Regex(
            """(?i)(?:={2,}\s*CURRENT\s+REQUEST|CURRENT\s+REQUEST\s*={2,}|={2,}\s*LEARNER\s+PERSONALIZATION|LEARNER\s+PERSONALIZATION\s*(?:CONTEXT\s*)?={2,}|\bLEARNER\s+PERSONALIZATION\s+CONTEXT\b|={3,}|\bselected\s+answer\s*[:=]|\bexpected\s+answer\s*[:=])"""
        )

        private val COMMAND_VERB_PREFIX_PATTERN = Regex(
            """^(?:write|tell|calculate|summarize|list|explain|generate|provide|create|produce|enhance|simplify|convert|translate|give|show|make|act\s+as|please|can\s+you|i\s+want)\b""",
            RegexOption.IGNORE_CASE
        )
    }

    /**
     * Extracts a clean, deterministic topic from [rawPrompt].
     *
     * Supported formats:
     * 1. Prompt wrapped with '=== CURRENT REQUEST ===' containing documented system templates.
     * 2. Direct template sentences matching documented request patterns.
     * 3. Clean direct topic titles (e.g. "Kotlin Coroutines", "StateFlow vs SharedFlow", "C# async/await").
     *
     * If the input is empty, malformed, an instruction injection, or an unknown prompt format,
     * it safely resolves to [FALLBACK_TOPIC].
     */
    internal fun extractCleanTopic(rawPrompt: String): String {
        if (rawPrompt.isBlank()) return FALLBACK_TOPIC

        val hasStandardRequestWrapper = rawPrompt.contains("=== CURRENT REQUEST ===")

        val body = if (hasStandardRequestWrapper) {
            rawPrompt.substringAfter("=== CURRENT REQUEST ===").substringBefore("===").trim()
        } else {
            rawPrompt.trim()
        }

        if (body.isBlank()) return FALLBACK_TOPIC

        // Guard against explicit instruction prefixes, prompt injections, unknown labels, and prohibited secrets
        if (INSTRUCTION_PREFIX_PATTERN.containsMatchIn(body) ||
            INJECTION_COMMAND_PATTERN.containsMatchIn(body) ||
            UNKNOWN_OR_MALFORMED_PROMPT_PATTERN.containsMatchIn(body) ||
            SECRET_OR_SENTINEL_PATTERN.containsMatchIn(body)
        ) {
            return FALLBACK_TOPIC
        }

        val extracted = extractFromKnownTemplates(body)
        if (extracted != null) {
            return finalizeTopic(extracted)
        }

        // If the prompt does NOT have a recognized template and contains internal metadata or delimiters,
        // it must not be treated as an arbitrary direct topic.
        if (PROMPT_DELIMITER_OR_METADATA_PATTERN.containsMatchIn(body)) {
            return FALLBACK_TOPIC
        }

        // Direct topic evaluation
        if (isValidDirectTopic(body)) {
            return finalizeTopic(body)
        }

        return FALLBACK_TOPIC
    }

    private fun extractQuotedValue(prefix: String, text: String): String? {
        val single = Regex("""$prefix\s*[:=]?\s*'([^']+)'""", RegexOption.IGNORE_CASE).find(text)
        if (single != null) return single.groupValues[1].trim()
        val double = Regex("""$prefix\s*[:=]?\s*"([^"]+)"""", RegexOption.IGNORE_CASE).find(text)
        if (double != null) return double.groupValues[1].trim()
        return null
    }

    private fun extractInnerQuoted(text: String): String? {
        val double = Regex(""""([^"]+)"""").find(text)
        if (double != null) return double.groupValues[1].trim()
        val single = Regex("""'([^']+)'""").find(text)
        if (single != null) return single.groupValues[1].trim()
        return null
    }

    private fun extractFromKnownTemplates(text: String): String? {
        // Template 1: Question pattern (e.g. "Explain the reasoning for the question: '...'")
        val rawQuestion = extractQuotedValue(
            """(?:Explain the reasoning for the question|educational feedback for question|question)""",
            text
        )
        if (rawQuestion != null) {
            val innerQuoted = extractInnerQuoted(rawQuestion)
            if (innerQuoted != null) {
                return innerQuoted
            }
            val ofMatch = Regex("""\b(?:of|on|about)\s+([A-Za-z0-9 _&+\-/#.`"'\\]+)$""", RegexOption.IGNORE_CASE).find(rawQuestion)
            if (ofMatch != null) {
                return ofMatch.groupValues[1].trim()
            }
            return rawQuestion
        }

        // Template 2: Detailed explanation / expanding on
        val expanding = extractQuotedValue(
            """(?:expanding on|detailed explanation on)""",
            text
        )
        if (expanding != null) {
            return expanding
        }

        // Template 3: Quiz generation
        val quiz = extractQuotedValue(
            """(?:evaluation quiz for|quiz for)\s*(?:topic:)?""",
            text
        )
        if (quiz != null) {
            return quiz
        }

        // Template 4: Lesson generation
        val lesson = extractQuotedValue(
            """(?:lesson covering topic|lesson for technical card|micro-lesson for topic|microlearning lesson about topic|lesson about topic)\s*(?:topic:)?""",
            text
        )
        if (lesson != null) {
            return lesson
        }

        // Template 5: Code examples demonstrating
        val example = extractQuotedValue(
            """demonstrating""",
            text
        )
        if (example != null) {
            return example
        }

        // Template 6: Studying recommendation
        val studyingMatch = Regex(
            """studying\s+([^.']+)is recommended""",
            RegexOption.IGNORE_CASE
        ).find(text)
        if (studyingMatch != null) {
            return studyingMatch.groupValues[1].trim()
        }

        // Template 7: Difficulty transition
        val difficultyMatch = Regex(
            """difficulty transition to\s+([A-Za-z0-9_]+)""",
            RegexOption.IGNORE_CASE
        ).find(text)
        if (difficultyMatch != null) {
            return difficultyMatch.groupValues[1].trim()
        }

        // Template 8: Insight title
        val insight = extractQuotedValue(
            """learning insight title""",
            text
        )
        if (insight != null) {
            return insight
        }

        // Template 9: Explicit topic tag
        val topicTag = extractQuotedValue(
            """topic""",
            text
        )
        if (topicTag != null) {
            return topicTag
        }

        return null
    }

    private fun isValidDirectTopic(text: String): Boolean {
        // Direct topics must not be multiline
        if (text.lines().count { it.isNotBlank() } > 1) return false

        // Direct topics must not contain prompt delimiters or metadata
        if (PROMPT_DELIMITER_OR_METADATA_PATTERN.containsMatchIn(text)) return false

        // Direct topics must not start with arbitrary command verbs
        if (COMMAND_VERB_PREFIX_PATTERN.containsMatchIn(text)) return false

        // Check for unbalanced outer quotes
        if ((text.startsWith("'") && !text.endsWith("'")) || (text.startsWith("\"") && !text.endsWith("\""))) {
            return false
        }
        if ((!text.startsWith("'") && text.endsWith("'")) || (!text.startsWith("\"") && text.endsWith("\""))) {
            return false
        }

        return true
    }

    private fun finalizeTopic(candidate: String): String {
        var cleaned = candidate.trim()

        // Strip matching outer quotes if present
        if ((cleaned.startsWith("'") && cleaned.endsWith("'")) ||
            (cleaned.startsWith("\"") && cleaned.endsWith("\""))
        ) {
            if (cleaned.length >= 2) {
                cleaned = cleaned.substring(1, cleaned.length - 1).trim()
            }
        }

        // Guard against any injection or secret in extracted candidate
        if (cleaned.isBlank() ||
            INJECTION_COMMAND_PATTERN.containsMatchIn(cleaned) ||
            SECRET_OR_SENTINEL_PATTERN.containsMatchIn(cleaned) ||
            INSTRUCTION_PREFIX_PATTERN.containsMatchIn(cleaned) ||
            PROMPT_DELIMITER_OR_METADATA_PATTERN.containsMatchIn(cleaned)
        ) {
            return FALLBACK_TOPIC
        }

        // Remove unprintable control characters while preserving valid technical symbols, quotes, backslashes, Unicode
        val sanitizedChars = cleaned.filter { it >= ' ' || it == '\t' }.trim()

        if (sanitizedChars.isBlank()) return FALLBACK_TOPIC

        return if (sanitizedChars.length > MAX_TOPIC_LENGTH) {
            sanitizedChars.take(MAX_TOPIC_LENGTH).trim()
        } else {
            sanitizedChars
        }
    }

    internal fun sanitizeContext(rawContext: String, fallback: String): String {
        if (rawContext.isBlank()) return fallback

        // If context contains internal prompt delimiters, injections, or secrets, use safe fallback
        if (PROMPT_DELIMITER_OR_METADATA_PATTERN.containsMatchIn(rawContext) ||
            INJECTION_COMMAND_PATTERN.containsMatchIn(rawContext) ||
            SECRET_OR_SENTINEL_PATTERN.containsMatchIn(rawContext)
        ) {
            return fallback
        }

        // Clean control characters (preserve normal newlines and tabs)
        val cleaned = rawContext
            .filter { it >= ' ' || it == '\n' || it == '\r' || it == '\t' }
            .trim()

        if (cleaned.isBlank()) return fallback

        return if (cleaned.length > MAX_CONTEXT_LENGTH) {
            cleaned.take(MAX_CONTEXT_LENGTH).trim()
        } else {
            cleaned
        }
    }

    fun createExplanationResponse(request: AIRequest, config: MockAIConfiguration): AIResponse {
        val topic = extractCleanTopic(request.prompt)
        val safeContext = sanitizeContext(request.context, fallback = DEFAULT_EXPLANATION_CONTEXT)
        val content = """
            |### Explanation for: $topic
            |
            |**Key Concept Overview**:
            |$safeContext
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
        val safeContext = sanitizeContext(request.context, fallback = DEFAULT_LESSON_CONTEXT)
        val content = """
            |# Micro-Lesson: $topic
            |
            |## Objective
            |Master the key concepts behind $topic.
            |
            |## Core Breakdown
            |1. **Foundation**: $safeContext
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
        val payload = MockQuizPayload(
            question = "What is the primary architectural goal of $topic?",
            options = listOf(
                "Deterministic business logic execution",
                "Direct coupling to UI components",
                "Random non-repeatable state changes",
                "Bypassing repository abstractions"
            ),
            correctAnswerIndex = 0,
            explanation = "LearnWitMe mandates deterministic domain engines decoupled from UI and framework layers."
        )
        val content = json.encodeToString(MockQuizPayload.serializer(), payload)

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
