package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.ai.AIProvider
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.AIResponse
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.KnowledgePackageRepository
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.service.Clock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GenerateAIKnowledgeCardWorkflowTest {

    private val aiProvider = mockk<AIProvider>()
    private val knowledgeRepository = mockk<KnowledgeRepository>(relaxed = true)
    private val knowledgePackageRepository = mockk<KnowledgePackageRepository>(relaxed = true)
    private val clock = object : Clock {
        override fun currentTimeMillis(): Long = 1700000000000L
    }

    private lateinit var workflow: GenerateAIKnowledgeCardWorkflow

    private val samplePackageId = KnowledgePackageId("pkg_default")
    private val samplePackage = KnowledgePackage(
        id = samplePackageId,
        title = "Core Package",
        description = "Core learning package",
        version = 1,
        checksum = "chk",
        isCore = true,
        status = PackageStatus.PUBLISHED,
        topics = emptyList(),
        cards = emptyList()
    )

    @Before
    fun setup() {
        workflow = GenerateAIKnowledgeCardWorkflow(
            aiProvider = aiProvider,
            knowledgeRepository = knowledgeRepository,
            knowledgePackageRepository = knowledgePackageRepository,
            clock = clock
        )
        coEvery { knowledgePackageRepository.getPackage(samplePackageId) } returns samplePackage
    }

    @Test
    fun testGenerateAIKnowledgeCard_success_createsAndPersistsCard() = runBlocking {
        val validJson = """
            {
              "title": "Kotlin Flows",
              "summary": "Asynchronous data stream emitting sequentially.",
              "whyItMatters": "Enables reactive programming in modern Android.",
              "realWorldExample": "StateFlow in ViewModel observing Room updates.",
              "commonMistakes": "Collecting Flow in Compose without lifecycle awareness.",
              "whenNotToUse": "Simple one-shot async values (use suspend instead).",
              "bestPractice": "Use collectAsStateWithLifecycle in Jetpack Compose.",
              "careerImpact": "Required for Mid-Senior Android roles.",
              "estimatedReadingTimeSeconds": 180,
              "references": ["Kotlin Docs"],
              "officialDocumentationLink": "https://kotlinlang.org/docs/flow.html"
            }
        """.trimIndent()

        coEvery { aiProvider.generateLesson(any()) } returns AIResponse(
            content = validJson,
            tokensUsed = 150,
            modelId = "gemini-1.5-pro",
            executionTimeMs = 500L
        )

        val result = workflow.execute(samplePackageId, "tp_kotlin", DifficultyLevel.MID)

        assertTrue(result is AIKnowledgeGenerationResult.Success)
        val card = (result as AIKnowledgeGenerationResult.Success).card
        assertEquals("Kotlin Flows", card.title)
        assertEquals("tp_kotlin", card.relatedTopicCodes.first())
        assertEquals(DifficultyLevel.MID, card.difficulty)
        assertEquals(samplePackageId, card.packageId)
        assertTrue(card.tags.contains(Tag("ai_generated")))
        assertTrue(card.tags.contains(Tag("model_gemini-1.5-pro")))

        coVerify { knowledgeRepository.saveCard(card) }
        coVerify { knowledgePackageRepository.savePackage(match { it.cards.contains(card) }) }
    }

    @Test
    fun testGenerateAIKnowledgeCard_malformedJson_returnsFailure() = runBlocking {
        coEvery { aiProvider.generateLesson(any()) } returns AIResponse(
            content = "This is not JSON",
            tokensUsed = 50,
            modelId = "gemini-1.5-pro",
            executionTimeMs = 300L
        )

        val result = workflow.execute(samplePackageId, "tp_kotlin", DifficultyLevel.BEGINNER)
        assertTrue(result is AIKnowledgeGenerationResult.Failure)
        assertTrue((result as AIKnowledgeGenerationResult.Failure).reason.contains("parse"))
    }

    @Test
    fun testGenerateAIKnowledgeCard_blankRequiredFields_returnsFailure() = runBlocking {
        val invalidJson = """
            {
              "title": "",
              "summary": "",
              "whyItMatters": ""
            }
        """.trimIndent()

        coEvery { aiProvider.generateLesson(any()) } returns AIResponse(
            content = invalidJson,
            tokensUsed = 20,
            modelId = "gemini-1.5-pro",
            executionTimeMs = 200L
        )

        val result = workflow.execute(samplePackageId, "tp_kotlin", DifficultyLevel.BEGINNER)
        assertTrue(result is AIKnowledgeGenerationResult.Failure)
        assertTrue((result as AIKnowledgeGenerationResult.Failure).reason.contains("Validation failed"))
    }

    @Test
    fun testGenerateAIKnowledgeCard_duplicateTitle_returnsFailure() = runBlocking {
        val existingCard = KnowledgeCard(
            id = KnowledgeCardId("c_existing"),
            packageId = samplePackageId,
            title = "Kotlin Flows",
            summary = "...",
            whyItMatters = "...",
            realWorldExample = "...",
            commonMistakes = "...",
            whenNotToUse = "...",
            bestPractice = "...",
            reference = emptyList(),
            officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER,
            estimatedReadingTimeSeconds = 120,
            careerImpact = "",
            tags = emptyList(),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = listOf("tp_kotlin")
        )

        val pkgWithCard = samplePackage.copy(cards = listOf(existingCard))
        coEvery { knowledgePackageRepository.getPackage(samplePackageId) } returns pkgWithCard

        val validJson = """
            {
              "title": "Kotlin Flows",
              "summary": "Summary",
              "whyItMatters": "Why",
              "realWorldExample": "Ex",
              "commonMistakes": "Mistake",
              "whenNotToUse": "When",
              "bestPractice": "Best"
            }
        """.trimIndent()

        coEvery { aiProvider.generateLesson(any()) } returns AIResponse(
            content = validJson,
            tokensUsed = 50,
            modelId = "gemini-1.5-pro",
            executionTimeMs = 200L
        )

        val result = workflow.execute(samplePackageId, "tp_kotlin", DifficultyLevel.BEGINNER)
        assertTrue(result is AIKnowledgeGenerationResult.Failure)
        assertTrue((result as AIKnowledgeGenerationResult.Failure).reason.contains("Duplicate"))
    }

    @Test
    fun testGenerateAIKnowledgeCard_providerException_returnsFailure() = runBlocking {
        coEvery { aiProvider.generateLesson(any()) } throws RuntimeException("Network timeout")

        val result = workflow.execute(samplePackageId, "tp_kotlin", DifficultyLevel.BEGINNER)
        assertTrue(result is AIKnowledgeGenerationResult.Failure)
        assertTrue((result as AIKnowledgeGenerationResult.Failure).reason.contains("Network timeout"))
    }
}
