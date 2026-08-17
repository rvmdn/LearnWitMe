package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.PatternDifficultyLevel
import com.learnwitme.core.domain.model.memory.StudyTimeWindow
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AILearningInteractionServiceTest {

    private val aiManager = mockk<AIManager>()
    private lateinit var service: AILearningInteractionService

    private val sampleUserId = UserId("usr_service_1")
    private val otherUserId = UserId("usr_service_2")
    private val sampleCardId = KnowledgeCardId("card_001")
    private val sampleTopic = Topic("tp_kotlin", "Kotlin Basics", "Fundamental Kotlin Concepts")

    private val sampleCard = KnowledgeCard(
        id = sampleCardId,
        content = KnowledgeContent(
            title = "Kotlin Coroutines",
            summary = "Asynchronous programming framework in Kotlin",
            whyItMatters = "Simplifies asynchronous programming",
            realWorldExample = "Fetching network data asynchronously",
            commonMistakes = "Blocking main thread inside coroutines",
            bestPractice = "Use structured concurrency"
        ),
        difficulty = DifficultyLevel.BEGINNER,
        relatedTopicCodes = listOf("tp_kotlin")
    )

    private val defaultAIResponse = AIResponse(
        content = """
            # Kotlin Coroutines
            
            Kotlin Coroutines simplify asynchronous execution.
            
            Q: What is a coroutine?
            A) Lightweight thread (Correct)
            B) Heavy OS process
            Explanation: Coroutines are user-space threads.
        """.trimIndent(),
        tokensUsed = 150,
        finishReason = "STOP",
        modelId = "gemini-1.5-pro",
        executionTimeMs = 200L
    )

    private val samplePersonalizationContext = LearningPersonalizationContext(
        userId = sampleUserId,
        memoryId = LearningMemoryId("mem_001"),
        profile = LearningPersonalizationProfile(
            primaryStyle = LearningStyleType.VISUAL,
            secondaryStyle = null,
            preferenceConfidence = 0.90,
            preferenceObservationCount = 12,
            bestStudyWindow = StudyTimeWindow.MORNING,
            preferredDifficulty = PatternDifficultyLevel.BEGINNER,
            averageSessionDurationMinutes = 25.0,
            completionRate = 0.90,
            totalSessionsAnalyzed = 12
        ),
        strengths = emptyList(),
        weaknesses = emptyList(),
        behaviors = emptyList(),
        overallRetentionAverage = 0.85,
        totalCardsTracked = 10,
        activeGoals = listOf("Master Kotlin"),
        generatedEpochMs = 1000L
    )

    private fun createSampleMemory(initialConfidence: Double = 0.50, userId: UserId = sampleUserId): LearningMemory {
        val record = MemoryRecord(
            cardId = sampleCardId,
            state = MemoryState.LEARNING,
            retention = 0.80,
            stability = 1.0,
            confidence = initialConfidence
        )
        return LearningMemory(
            id = LearningMemoryId("mem_001"),
            userId = userId,
            records = listOf(record)
        )
    }

    @Before
    fun setUp() {
        coEvery { aiManager.generateLesson(any(), any()) } returns defaultAIResponse
        coEvery { aiManager.generateQuiz(any(), any()) } returns defaultAIResponse
        coEvery { aiManager.generateExplanation(any(), any()) } returns defaultAIResponse
        coEvery { aiManager.simplifyContent(any(), any()) } returns defaultAIResponse

        service = AILearningInteractionService(aiManager)
    }

    // 1. Lesson Interaction
    @Test
    fun testExecuteLessonWithFeedbackAndMemory() = runBlocking {
        val initialMemory = createSampleMemory(initialConfidence = 0.50)
        val feedback = AILearningFeedback(
            interactionId = "int_lesson_01",
            usefulnessRating = 5,
            confidenceSignal = 0.85,
            comment = "Great structured lesson",
            createdAt = 2000L
        )

        val result = service.executeLesson(
            interactionId = "int_lesson_01",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1800L,
            cardId = sampleCardId,
            topicModel = sampleTopic,
            personalizationContext = samplePersonalizationContext,
            policy = PersonalizedPromptPolicy.LESSON_ONLY,
            feedback = feedback,
            currentMemory = initialMemory,
            provider = AIProviderType.GEMINI,
            model = "gemini-1.5-pro",
            promptTokens = 100,
            completionTokens = 50
        )

        assertNotNull(result.generatedResult)
        assertEquals("Kotlin Coroutines", result.generatedResult.title)
        assertEquals("int_lesson_01", result.interaction.interactionId)
        assertEquals(AIInteractionType.LESSON, result.interaction.action)
        assertEquals(sampleCardId, result.interaction.cardId)
        assertEquals("LESSON_ONLY", result.interaction.personalizationPreset)
        assertEquals(150, result.interaction.totalTokens)
        assertEquals(AIOutcomeStatus.COMPLETED, result.outcome?.status)

        assertTrue(result.isFeedbackAccepted)
        assertTrue(result.isMemoryIntegrated)
        assertNotNull(result.updatedMemory)

        val updatedRecord = result.updatedMemory?.findRecord(sampleCardId)
        assertEquals(0.60, updatedRecord!!.confidence, 0.001)

        coVerify { aiManager.generateLesson(match { it.prompt.contains("Kotlin Coroutines") }, any()) }
    }

    // 2. Quiz Interaction (Card & Topic)
    @Test
    fun testExecuteQuizForCardWithFeedback() = runBlocking {
        val memory = createSampleMemory(initialConfidence = 0.50)
        val feedback = AILearningFeedback(
            interactionId = "int_quiz_card_01",
            usefulnessRating = 4,
            confidenceSignal = 0.80,
            createdAt = 1900L
        )

        val result = service.executeQuiz(
            interactionId = "int_quiz_card_01",
            userId = sampleUserId,
            card = sampleCard,
            startedAt = 1000L,
            completedAt = 1800L,
            feedback = feedback,
            currentMemory = memory
        )

        assertNotNull(result.generatedResult)
        assertEquals(AIInteractionType.QUIZ, result.interaction.action)
        assertEquals(sampleCardId, result.interaction.cardId)
        assertTrue(result.isFeedbackAccepted)
        assertTrue(result.isMemoryIntegrated)
        coVerify { aiManager.generateQuiz(match { it.prompt.contains("Kotlin Coroutines") }, any()) }
    }

    @Test
    fun testExecuteQuizForTopicWithoutFeedback() = runBlocking {
        val result = service.executeQuiz(
            interactionId = "int_quiz_topic_01",
            userId = sampleUserId,
            topic = "Kotlin Flow",
            startedAt = 1000L,
            completedAt = 1500L
        )

        assertNotNull(result.generatedResult)
        assertEquals(AIInteractionType.QUIZ, result.interaction.action)
        assertNull(result.feedback)
        assertNull(result.feedbackResult)
        assertNull(result.memoryIntegrationResult)
        assertNull(result.updatedMemory)
    }

    // 3. Explanation Interaction
    @Test
    fun testExecuteExplanationWithFeedback() = runBlocking {
        val memory = createSampleMemory(initialConfidence = 0.50)
        val feedback = AILearningFeedback(
            interactionId = "int_explain_01",
            usefulnessRating = 5,
            confidenceSignal = 0.90,
            createdAt = 1800L
        )

        val result = service.executeExplanation(
            interactionId = "int_explain_01",
            userId = sampleUserId,
            question = "What is a coroutine?",
            selectedAnswer = "A lightweight thread",
            expectedAnswer = "A lightweight thread",
            startedAt = 1000L,
            completedAt = 1600L,
            cardId = sampleCardId,
            feedback = feedback,
            currentMemory = memory
        )

        assertNotNull(result.generatedResult)
        assertEquals(AIInteractionType.EXPLANATION, result.interaction.action)
        assertTrue(result.isFeedbackAccepted)
        assertTrue(result.isMemoryIntegrated)
    }

    // 4. Knowledge Expansion Interaction
    @Test
    fun testExecuteKnowledgeExpansionWithFeedback() = runBlocking {
        val memory = createSampleMemory(initialConfidence = 0.50)
        val feedback = AILearningFeedback(
            interactionId = "int_expand_01",
            usefulnessRating = 4,
            createdAt = 1800L
        )

        val result = service.executeKnowledgeExpansion(
            interactionId = "int_expand_01",
            userId = sampleUserId,
            card = sampleCard,
            startedAt = 1000L,
            completedAt = 1600L,
            feedback = feedback,
            currentMemory = memory
        )

        assertNotNull(result.generatedResult)
        assertEquals(sampleCard, result.generatedResult.originalCard)
        assertEquals(AIInteractionType.KNOWLEDGE_EXPANSION, result.interaction.action)
        assertTrue(result.isFeedbackAccepted)
    }

    // 5. Simplification Interaction
    @Test
    fun testExecuteSimplificationWithFeedback() = runBlocking {
        val memory = createSampleMemory(initialConfidence = 0.50)
        val feedback = AILearningFeedback(
            interactionId = "int_simple_01",
            usefulnessRating = 5,
            confidenceSignal = 0.85,
            createdAt = 1800L
        )

        val result = service.executeSimplification(
            interactionId = "int_simple_01",
            userId = sampleUserId,
            technicalContent = "Coroutines run asynchronously with non-blocking suspend functions",
            startedAt = 1000L,
            completedAt = 1600L,
            cardId = sampleCardId,
            feedback = feedback,
            currentMemory = memory
        )

        assertNotNull(result.generatedResult)
        assertEquals(AIInteractionType.SIMPLIFICATION, result.interaction.action)
        assertTrue(result.isFeedbackAccepted)
        assertTrue(result.isMemoryIntegrated)
    }

    // 6. No Feedback Scenario -> No memory projection
    @Test
    fun testNoFeedbackProvidedDoesNotModifyMemory() = runBlocking {
        val initialMemory = createSampleMemory(initialConfidence = 0.50)

        val result = service.executeLesson(
            interactionId = "int_no_feedback_01",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1500L,
            feedback = null,
            currentMemory = initialMemory
        )

        assertNotNull(result.generatedResult)
        assertNull(result.feedback)
        assertNull(result.feedbackResult)
        assertNull(result.memoryIntegrationResult)
        assertNull(result.updatedMemory)
    }

    // 7. Feedback Validation Failure -> Preserves result, rejects feedback, no memory modification
    @Test
    fun testFeedbackValidationFailurePreservesResultAndDoesNotModifyMemory() = runBlocking {
        val initialMemory = createSampleMemory(initialConfidence = 0.50)
        val invalidFeedback = AILearningFeedback(
            interactionId = "int_mismatched_id",
            usefulnessRating = 5,
            createdAt = 2000L
        )

        val result = service.executeLesson(
            interactionId = "int_lesson_target",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1500L,
            feedback = invalidFeedback,
            currentMemory = initialMemory
        )

        assertNotNull(result.generatedResult)
        assertTrue(result.isFeedbackRejected)
        assertFalse(result.isFeedbackAccepted)
        assertFalse(result.isMemoryIntegrated)
        assertNull(result.updatedMemory)
    }

    // 8. Memory Integration Rejection (e.g. User ID mismatch) -> Preserves feedback, rejects integration, no memory modification
    @Test
    fun testMemoryIntegrationRejectionOnUserIdMismatch() = runBlocking {
        val otherMemory = createSampleMemory(initialConfidence = 0.50, userId = otherUserId)
        val feedback = AILearningFeedback(
            interactionId = "int_user_test",
            usefulnessRating = 5,
            createdAt = 2000L
        )

        val result = service.executeLesson(
            interactionId = "int_user_test",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1500L,
            feedback = feedback,
            currentMemory = otherMemory
        )

        assertNotNull(result.generatedResult)
        assertTrue(result.isFeedbackAccepted)
        assertFalse(result.isMemoryIntegrated)
        assertNull(result.updatedMemory)
    }

    // 9. Preserves metadata (provider, model, tokens, personalization)
    @Test
    fun testPreservesMetadataCorrectly() = runBlocking {
        val result = service.executeLesson(
            interactionId = "int_meta_01",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1500L,
            provider = AIProviderType.GEMINI,
            model = "gemini-1.5-flash",
            promptTokens = 250,
            completionTokens = 120,
            policy = PersonalizedPromptPolicy.CONCISE
        )

        assertEquals(AIProviderType.GEMINI, result.interaction.provider)
        assertEquals("gemini-1.5-flash", result.interaction.model)
        assertEquals(250, result.interaction.promptTokens)
        assertEquals(120, result.interaction.completionTokens)
        assertEquals(370, result.interaction.totalTokens)
        assertEquals("CONCISE", result.interaction.personalizationPreset)
    }

    // 10. Immutability of original memory
    @Test
    fun testOriginalMemoryRemainsImmutable() = runBlocking {
        val originalMemory = createSampleMemory(initialConfidence = 0.50)
        val originalMemoryCopy = originalMemory.copy()

        val feedback = AILearningFeedback(
            interactionId = "int_immutability",
            usefulnessRating = 5,
            confidenceSignal = 0.90,
            createdAt = 1800L
        )

        val result = service.executeLesson(
            interactionId = "int_immutability",
            userId = sampleUserId,
            topic = "Kotlin Coroutines",
            startedAt = 1000L,
            completedAt = 1500L,
            cardId = sampleCardId,
            feedback = feedback,
            currentMemory = originalMemory
        )

        assertEquals(originalMemoryCopy, originalMemory)
        assertEquals(0.50, originalMemory.findRecord(sampleCardId)!!.confidence, 0.001)
        assertEquals(0.60, result.updatedMemory!!.findRecord(sampleCardId)!!.confidence, 0.001)
    }
}
