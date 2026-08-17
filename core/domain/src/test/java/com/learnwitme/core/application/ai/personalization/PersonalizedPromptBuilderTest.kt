package com.learnwitme.core.application.ai.personalization

import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.PatternDifficultyLevel
import com.learnwitme.core.domain.model.memory.StudyTimeWindow
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizedPromptBuilderTest {

    private val builder = LearningPromptContextBuilder()

    private val sampleProfile = LearningPersonalizationProfile(
        primaryStyle = LearningStyleType.VISUAL,
        secondaryStyle = null,
        preferenceConfidence = 0.85,
        preferenceObservationCount = 10,
        bestStudyWindow = StudyTimeWindow.MORNING,
        preferredDifficulty = PatternDifficultyLevel.BEGINNER,
        averageSessionDurationMinutes = 20.0,
        completionRate = 0.85,
        totalSessionsAnalyzed = 10
    )

    private val sampleWeakness = LearningWeakness(
        topicCode = "tp_async",
        topicName = "Asynchronous Flows",
        averageRetention = 0.45,
        failureRate = 0.40,
        forgottenCardCount = 2,
        totalCardCount = 5,
        consecutiveFailureCount = 2,
        severity = WeaknessSeverity.MODERATE,
        detectedEpochMs = 1000L
    )

    private val sampleContext = LearningPersonalizationContext(
        userId = UserId("usr_001"),
        memoryId = LearningMemoryId("mem_001"),
        profile = sampleProfile,
        strengths = emptyList(),
        weaknesses = listOf(sampleWeakness),
        behaviors = emptyList(),
        overallRetentionAverage = 0.78,
        totalCardsTracked = 15,
        activeGoals = listOf("Master Kotlin Coroutines"),
        generatedEpochMs = 1000L
    )

    @Test
    fun testBuildPromptWithFullPolicy() {
        val prompt = builder.buildPrompt(
            requestPrompt = "Explain Kotlin Flow",
            personalizationContext = sampleContext,
            policy = PersonalizedPromptPolicy.FULL
        )

        assertNotNull(prompt)
        assertTrue(prompt.userPrompt.contains("VISUAL"))
        assertTrue(prompt.userPrompt.contains("Asynchronous Flows"))
        assertTrue(prompt.userPrompt.contains("Explain Kotlin Flow"))
        assertEquals("usr_001", prompt.metadata["userId"])
    }

    @Test
    fun testStrictPrivacyPolicyOmitsWeaknessesAndRetention() {
        val prompt = builder.buildPrompt(
            requestPrompt = "Explain Kotlin Flow",
            personalizationContext = sampleContext,
            policy = PersonalizedPromptPolicy.STRICT_PRIVACY
        )

        assertNotNull(prompt)
        assertTrue(prompt.userPrompt.contains("VISUAL"))
        assertFalse(prompt.userPrompt.contains("Asynchronous Flows"))
        assertFalse(prompt.userPrompt.contains("Retention Rate"))
    }

    @Test
    fun testBuildPromptWithoutPersonalizationContext() {
        val prompt = builder.buildPrompt(
            requestPrompt = "Explain Kotlin Flow",
            personalizationContext = null
        )

        assertNotNull(prompt)
        assertTrue(prompt.userPrompt.contains("Explain Kotlin Flow"))
        assertFalse(prompt.userPrompt.contains("LEARNER PERSONALIZATION CONTEXT"))
    }
}
