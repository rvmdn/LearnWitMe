package com.learnwitme.core.application.usecase.ai

import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AILearningUseCasesTest {

    private val aiManager = mockk<AIManager>()
    private val defaultResponse = AIResponse(
        content = """
            # Kotlin Coroutines
            
            Kotlin Coroutines simplify asynchronous programming.
            
            Q: What is a Coroutine?
            A) A light-weight thread (Correct)
            B) A heavy process
            Explanation: Coroutines are lightweight.
        """.trimIndent(),
        tokensUsed = 100,
        finishReason = "STOP",
        modelId = "mock-model",
        executionTimeMs = 150L
    )

    private val sampleCard = KnowledgeCard(
        id = KnowledgeCardId("card_01"),
        content = KnowledgeContent(
            title = "Kotlin Coroutines",
            summary = "Asynchronous programming framework in Kotlin",
            whyItMatters = "Simplifies asynchronous programming",
            realWorldExample = "Fetching network data without blocking UI",
            commonMistakes = "Blocking main thread inside coroutine",
            bestPractice = "Use appropriate CoroutineDispatcher"
        ),
        difficulty = DifficultyLevel.BEGINNER,
        relatedTopicCodes = listOf("tp_kotlin", "tp_coroutines")
    )

    @Before
    fun setUp() {
        coEvery { aiManager.generateLesson(any(), any()) } returns defaultResponse
        coEvery { aiManager.generateQuiz(any(), any()) } returns defaultResponse
        coEvery { aiManager.generateExplanation(any(), any()) } returns defaultResponse
        coEvery { aiManager.simplifyContent(any(), any()) } returns defaultResponse
    }

    @Test
    fun testGenerateLessonUseCase() = runBlocking {
        val useCase = GenerateLessonUseCase(aiManager)
        val lesson = useCase("Kotlin Flow")

        assertNotNull(lesson)
        assertEquals("Kotlin Coroutines", lesson.title)
        coVerify { aiManager.generateLesson(match { it.prompt.contains("Kotlin Flow") }, any()) }
    }

    @Test
    fun testGenerateQuizUseCaseFromCard() = runBlocking {
        val useCase = GenerateQuizUseCase(aiManager)
        val quiz = useCase(sampleCard)

        assertNotNull(quiz)
        assertEquals("Kotlin Coroutines", quiz.title)
        coVerify { aiManager.generateQuiz(match { it.prompt.contains("Kotlin Coroutines") }, any()) }
    }

    @Test
    fun testExplainAnswerUseCase() = runBlocking {
        val useCase = ExplainAnswerUseCase(aiManager)
        val explanation = useCase(
            question = "What is a StateFlow?",
            selectedAnswer = "A cold stream",
            expectedAnswer = "A state-holding hot Flow"
        )

        assertNotNull(explanation)
        assertEquals("Explanation for: What is a StateFlow?", explanation.title)
        coVerify { aiManager.generateExplanation(match { it.prompt.contains("What is a StateFlow?") }, any()) }
    }

    @Test
    fun testExpandKnowledgeCardUseCase() = runBlocking {
        val useCase = ExpandKnowledgeCardUseCase(aiManager)
        val expanded = useCase(sampleCard)

        assertNotNull(expanded)
        assertEquals(sampleCard, expanded.originalCard)
        coVerify { aiManager.generateLesson(match { it.prompt.contains("Kotlin Coroutines") }, any()) }
    }

    @Test
    fun testSimplifyTopicUseCase() = runBlocking {
        val useCase = SimplifyTopicUseCase(aiManager)
        val simplified = useCase("Monads are endofunctors on a category")

        assertNotNull(simplified)
        assertEquals("Monads are endofunctors on a category", simplified.original)
        coVerify { aiManager.simplifyContent(match { it.prompt.contains("Monads") }, any()) }
    }
}
