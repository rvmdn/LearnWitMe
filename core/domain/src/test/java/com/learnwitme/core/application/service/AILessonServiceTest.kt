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

class AILessonServiceTest {

    private val registry = AIProviderRegistry()
    private val router = AIProviderRouter(registry)
    private val aiManager = AIManager(router)
    private val lessonService = AILessonService(aiManager)

    @Before
    fun setUp() {
        registry.registerProvider(AIProviderType.MOCK, MockAIProvider())
    }

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Kotlin Generics", "Variance rules", "Why", "Example", "Mistake", "Not", "Best")
    )

    @Test
    fun testExpandCard() = runBlocking {
        val card = createCard("c1")
        val expanded = lessonService.expandCard(card)
        assertNotNull(expanded)
        assertTrue(expanded.contains("Micro-Lesson:"))
    }

    @Test
    fun testGenerateMicroLesson() = runBlocking {
        val lesson = lessonService.generateMicroLesson("StateFlow")
        assertNotNull(lesson)
        assertTrue(lesson.contains("StateFlow"))
    }

    @Test
    fun testProduceLearningExamples() = runBlocking {
        val card = createCard("c2")
        val examples = lessonService.produceLearningExamples(card)
        assertNotNull(examples)
        assertTrue(examples.contains("Explanation for:"))
    }
}
