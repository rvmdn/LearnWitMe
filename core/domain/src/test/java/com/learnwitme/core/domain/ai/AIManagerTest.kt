package com.learnwitme.core.domain.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AIManagerTest {

    private val registry = AIProviderRegistry()
    private val router = AIProviderRouter(registry)
    private val manager = AIManager(router)
    private val mockProvider = MockAIProvider()

    @Before
    fun setUp() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
    }

    @Test
    fun testGenerateExplanationOrchestration() = runBlocking {
        val request = AIRequest(prompt = "Clean Architecture")
        val response = manager.generateExplanation(request)

        assertNotNull(response)
        assertEquals("mock-ai-v1", response.modelId)
    }

    @Test
    fun testGenerateLessonOrchestration() = runBlocking {
        val request = AIRequest(prompt = "Room DAOs")
        val response = manager.generateLesson(request)

        assertNotNull(response)
        assertEquals("mock-ai-v1", response.modelId)
    }

    @Test
    fun testGenerateQuizOrchestration() = runBlocking {
        val request = AIRequest(prompt = "StateFlow vs SharedFlow")
        val response = manager.generateQuiz(request)

        assertNotNull(response)
        assertEquals("mock-ai-v1", response.modelId)
    }

    @Test
    fun testSimplifyContentOrchestration() = runBlocking {
        val request = AIRequest(prompt = "Dependency Injection")
        val response = manager.simplifyContent(request)

        assertNotNull(response)
        assertEquals("mock-ai-v1", response.modelId)
    }
}
