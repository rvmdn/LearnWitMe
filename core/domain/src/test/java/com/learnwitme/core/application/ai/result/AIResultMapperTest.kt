package com.learnwitme.core.application.ai.result

import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResultMapperTest {

    private val mapper = AIResultMapper()

    @Test
    fun testMapToGeneratedLesson() {
        val markdownContent = """
            # Kotlin StateFlow
            
            StateFlow is a state-holder observable flow.
            
            ## Overview
            StateFlow emits current and new state updates.
            
            ## Example
            val state = MutableStateFlow(0)
        """.trimIndent()

        val aiResponse = AIResponse(content = markdownContent, modelId = "gpt-4o-mini", executionTimeMs = 100L)
        val lesson = mapper.mapToGeneratedLesson(aiResponse, fallbackTitle = "Kotlin StateFlow")

        assertEquals("Kotlin StateFlow", lesson.title)
        assertTrue(lesson.summary.contains("StateFlow is a state-holder"))
        assertTrue(lesson.sections.isNotEmpty())
    }

    @Test
    fun testMapToGeneratedQuiz() {
        val markdownContent = """
            Q: What is a StateFlow in Kotlin?
            A) A cold stream
            B) A hot state-holding flow (Correct)
            C) A traditional callback
            Explanation: StateFlow is a hot flow that emits state updates.
        """.trimIndent()

        val aiResponse = AIResponse(content = markdownContent, modelId = "gpt-4o-mini", executionTimeMs = 100L)
        val quiz = mapper.mapToGeneratedQuiz(aiResponse, fallbackTitle = "StateFlow Quiz")

        assertEquals("StateFlow Quiz", quiz.title)
        assertTrue(quiz.questions.isNotEmpty())
        val question = quiz.questions.first()
        assertEquals("What is a StateFlow in Kotlin?", question.question)
        assertEquals("A hot state-holding flow", question.correctAnswer)
    }

    @Test
    fun testMapToGeneratedExplanation() {
        val aiResponse = AIResponse(content = "StateFlow holds state whereas SharedFlow emits events.", modelId = "gpt-4o-mini", executionTimeMs = 50L)
        val explanation = mapper.mapToGeneratedExplanation(aiResponse, fallbackTitle = "Flow Comparison")

        assertEquals("Flow Comparison", explanation.title)
        assertEquals("StateFlow holds state whereas SharedFlow emits events.", explanation.content)
    }

    @Test
    fun testMapToExpandedCard() {
        val sampleCard = KnowledgeCard(
            id = KnowledgeCardId("c1"),
            content = KnowledgeContent(
                title = "Coroutines",
                summary = "Async code",
                whyItMatters = "No thread blocking",
                realWorldExample = "API call",
                commonMistakes = "Blocking main",
                bestPractice = "Use Dispatchers.IO"
            ),
            difficulty = DifficultyLevel.BEGINNER,
            relatedTopicCodes = listOf("tp_kotlin")
        )

        val aiResponse = AIResponse(content = "Detailed expansion of Coroutines", modelId = "gpt-4o-mini", executionTimeMs = 80L)
        val expanded = mapper.mapToExpandedCard(aiResponse, sampleCard)

        assertEquals(sampleCard, expanded.originalCard)
        assertEquals("Detailed expansion of Coroutines", expanded.expandedContent)
    }

    @Test
    fun testMapToSimplifiedContent() {
        val aiResponse = AIResponse(content = "Coroutines are like mini tasks that don't freeze your app.", modelId = "gpt-4o-mini", executionTimeMs = 60L)
        val simplified = mapper.mapToSimplifiedContent(aiResponse, "Complex async description")

        assertEquals("Complex async description", simplified.original)
        assertEquals("Coroutines are like mini tasks that don't freeze your app.", simplified.simplified)
    }

    @Test(expected = AIResultParsingException::class)
    fun testBlankContentThrowsAIResultParsingException() {
        val aiResponse = AIResponse(content = "   ", modelId = "gpt-4o-mini", executionTimeMs = 10L)
        mapper.mapToGeneratedLesson(aiResponse)
    }
}
