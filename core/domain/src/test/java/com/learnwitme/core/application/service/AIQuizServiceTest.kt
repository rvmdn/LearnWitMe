package com.learnwitme.core.application.service

import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIProviderRegistry
import com.learnwitme.core.domain.ai.AIProviderRouter
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.ai.MockAIProvider
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIQuizServiceTest {

    private val registry = AIProviderRegistry()
    private val router = AIProviderRouter(registry)
    private val aiManager = AIManager(router)
    private val quizService = AIQuizService(aiManager)

    @Before
    fun setUp() {
        registry.registerProvider(AIProviderType.MOCK, MockAIProvider())
    }

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Dependency Injection", "Hilt scopes", "Why", "Example", "Mistake", "Not", "Best")
    )

    @Test
    fun testGenerateQuizFromCard() = runBlocking {
        val card = createCard("c_quiz")
        val quizJson = quizService.generateQuizFromCard(card)
        assertNotNull(quizJson)
        assertTrue(quizJson.contains("\"question\""))
        assertTrue(quizJson.contains("\"options\""))
    }

    @Test
    fun testExplainAnswer() = runBlocking {
        val feedback = quizService.explainAnswer("What is Hilt?", "DI Framework", true)
        assertNotNull(feedback)
        assertTrue(feedback.contains("Explanation for:"))
    }
}
