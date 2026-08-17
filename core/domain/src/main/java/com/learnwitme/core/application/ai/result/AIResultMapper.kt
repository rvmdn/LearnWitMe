package com.learnwitme.core.application.ai.result

import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Pure, deterministic, stateless mapper converting [AIResponse] payloads into domain-ready application result models.
 */
class AIResultMapper {

    fun mapToGeneratedLesson(response: AIResponse, fallbackTitle: String = "Generated Lesson"): GeneratedLesson {
        val rawContent = response.content.trim()
        if (rawContent.isBlank()) {
            throw AIResultParsingException("AI response content is blank for lesson generation")
        }

        val lines = rawContent.lines()
        val title = lines.firstOrNull { it.startsWith("#") }?.removePrefix("#")?.trim() ?: fallbackTitle
        val summary = lines.firstOrNull { it.isNotBlank() && !it.startsWith("#") } ?: rawContent.take(150)

        val sections = mutableListOf<LessonSection>()
        var currentSectionTitle = "Overview"
        val currentSectionContent = StringBuilder()

        for (line in lines) {
            if (line.startsWith("##")) {
                if (currentSectionContent.isNotBlank()) {
                    sections.add(LessonSection(currentSectionTitle, currentSectionContent.toString().trim()))
                    currentSectionContent.clear()
                }
                currentSectionTitle = line.removePrefix("##").trim()
            } else if (!line.startsWith("#")) {
                currentSectionContent.append(line).append("\n")
            }
        }
        if (currentSectionContent.isNotBlank()) {
            sections.add(LessonSection(currentSectionTitle, currentSectionContent.toString().trim()))
        }

        val examples = sections.filter { it.title.contains("Example", ignoreCase = true) }
            .map { it.content }
            .ifEmpty { listOf(rawContent) }

        return GeneratedLesson(
            title = title,
            summary = summary,
            sections = if (sections.isEmpty()) listOf(LessonSection("Lesson", rawContent)) else sections,
            examples = examples,
            estimatedReadingMinutes = (rawContent.split("\\s+".toRegex()).size / 150).coerceAtLeast(1)
        )
    }

    fun mapToGeneratedQuiz(response: AIResponse, fallbackTitle: String = "Generated Quiz"): GeneratedQuiz {
        val rawContent = response.content.trim()
        if (rawContent.isBlank()) {
            throw AIResultParsingException("AI response content is blank for quiz generation")
        }

        val questions = mutableListOf<QuizQuestion>()
        val lines = rawContent.lines()

        var currentQuestionText = ""
        var currentExplanation = ""
        val options = mutableListOf<QuizOption>()
        var optionCounter = 1

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("Q:") || trimmed.startsWith("Question:") || (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3."))) {
                if (currentQuestionText.isNotBlank() && options.isNotEmpty()) {
                    questions.add(
                        QuizQuestion(
                            question = currentQuestionText,
                            options = options.toList(),
                            correctAnswer = options.firstOrNull { it.isCorrect }?.text ?: options.first().text,
                            explanation = currentExplanation
                        )
                    )
                    options.clear()
                    optionCounter = 1
                    currentExplanation = ""
                }
                currentQuestionText = trimmed.replace(Regex("^(Q:|Question:|\\d+\\.)"), "").trim()
            } else if (trimmed.startsWith("A)") || trimmed.startsWith("B)") || trimmed.startsWith("C)") || trimmed.startsWith("D)") || trimmed.startsWith("-")) {
                val isCorrect = trimmed.contains("(Correct)", ignoreCase = true) || trimmed.contains("[Correct]", ignoreCase = true)
                val text = trimmed.replace(Regex("^[A-D]\\)|^-|\\(Correct\\)|\\[Correct\\]"), "").trim()
                options.add(QuizOption(id = "opt_$optionCounter", text = text, isCorrect = isCorrect))
                optionCounter++
            } else if (trimmed.startsWith("Explanation:")) {
                currentExplanation = trimmed.removePrefix("Explanation:").trim()
            }
        }

        if (currentQuestionText.isNotBlank()) {
            val finalOptions = if (options.isEmpty()) {
                listOf(
                    QuizOption("opt_1", "Option A", true),
                    QuizOption("opt_2", "Option B", false)
                )
            } else options.toList()

            questions.add(
                QuizQuestion(
                    question = currentQuestionText,
                    options = finalOptions,
                    correctAnswer = finalOptions.firstOrNull { it.isCorrect }?.text ?: finalOptions.first().text,
                    explanation = currentExplanation
                )
            )
        }

        if (questions.isEmpty()) {
            questions.add(
                QuizQuestion(
                    question = rawContent.take(100),
                    options = listOf(
                        QuizOption("opt_1", "True", true),
                        QuizOption("opt_2", "False", false)
                    ),
                    correctAnswer = "True",
                    explanation = rawContent
                )
            )
        }

        return GeneratedQuiz(
            title = fallbackTitle,
            questions = questions,
            estimatedDurationMinutes = (questions.size * 2).coerceAtLeast(2)
        )
    }

    fun mapToGeneratedExplanation(response: AIResponse, fallbackTitle: String = "Explanation"): GeneratedExplanation {
        val rawContent = response.content.trim()
        if (rawContent.isBlank()) {
            throw AIResultParsingException("AI response content is blank for explanation generation")
        }

        return GeneratedExplanation(
            title = fallbackTitle,
            content = rawContent,
            references = emptyList()
        )
    }

    fun mapToExpandedCard(response: AIResponse, card: KnowledgeCard): ExpandedKnowledgeCard {
        val rawContent = response.content.trim()
        if (rawContent.isBlank()) {
            throw AIResultParsingException("AI response content is blank for expanded knowledge card")
        }

        return ExpandedKnowledgeCard(
            originalCard = card,
            expandedContent = rawContent,
            examples = listOf(rawContent),
            relatedTopics = card.relatedTopicCodes
        )
    }

    fun mapToSimplifiedContent(response: AIResponse, originalText: String): SimplifiedContent {
        val rawContent = response.content.trim()
        if (rawContent.isBlank()) {
            throw AIResultParsingException("AI response content is blank for simplified content")
        }

        return SimplifiedContent(
            original = originalText,
            simplified = rawContent,
            readingLevel = "BEGINNER"
        )
    }
}
