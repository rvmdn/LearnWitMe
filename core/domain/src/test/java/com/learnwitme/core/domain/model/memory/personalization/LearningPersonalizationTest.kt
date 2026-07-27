package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.BehaviorType
import com.learnwitme.core.domain.model.memory.LearningBehavior
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStrength
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.PatternDifficultyLevel
import com.learnwitme.core.domain.model.memory.StudyTimeWindow
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPersonalizationTest {

    private val NOW = 1_700_000_000_000L
    private val userId = UserId("usr_test")
    private val memId = LearningMemoryId("mem_test")

    private fun sampleMemory(): LearningMemory {
        val pref = LearningPreference(
            primaryStyle = LearningStyleType.PRACTICAL,
            secondaryStyle = LearningStyleType.VISUAL,
            confidenceScore = 0.85,
            observationCount = 12,
            lastUpdatedEpochMs = NOW
        )
        val pattern = LearningPattern(
            bestStudyWindow = StudyTimeWindow.EVENING,
            averageSessionDurationMs = 1_200_000L, // 20 mins
            completionRate = 0.90,
            preferredDifficulty = PatternDifficultyLevel.INTERMEDIATE,
            sessionCount = 15,
            lastComputedEpochMs = NOW
        )
        val strengths = listOf(
            LearningStrength("tp_kotlin", "Kotlin Basics", 0.95, 0.90, 9, 10, NOW),
            LearningStrength("tp_android", "Android UI", 0.88, 0.82, 8, 10, NOW)
        )
        val weaknesses = listOf(
            LearningWeakness("tp_coroutines", "Coroutines", 0.40, 0.60, 4, 10, 3, WeaknessSeverity.SEVERE, NOW),
            LearningWeakness("tp_compose", "Jetpack Compose", 0.65, 0.35, 2, 10, 1, WeaknessSeverity.MILD, NOW)
        )
        val behaviors = listOf(
            LearningBehavior(BehaviorType.HIGH_HINT_USAGE, 5, 0.50, NOW, NOW),
            LearningBehavior(BehaviorType.FREQUENTLY_SKIPS_QUIZZES, 2, 0.20, NOW, NOW)
        )
        val records = listOf(
            MemoryRecord(KnowledgeCardId("kc_1"), MemoryState.MASTERED, 0.95, 5.0, 0.9, emptyList()),
            MemoryRecord(KnowledgeCardId("kc_2"), MemoryState.REVIEWING, 0.70, 2.0, 0.7, emptyList())
        )

        return LearningMemory(
            id = memId,
            userId = userId,
            records = records,
            preference = pref,
            pattern = pattern,
            strengths = strengths,
            weaknesses = weaknesses,
            behaviors = behaviors
        )
    }

    // ─────────────────────────────────────────────────────────────────
    // Builder & Profile Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `builder creates full personalization context using default policy`() {
        val memory = sampleMemory()
        val context = LearningPersonalizationBuilder.build(
            memory = memory,
            nowEpochMs = NOW
        )

        assertEquals(userId, context.userId)
        assertEquals(memId, context.memoryId)
        assertEquals(LearningStyleType.PRACTICAL, context.profile.primaryStyle)
        assertEquals(LearningStyleType.VISUAL, context.profile.secondaryStyle)
        assertEquals(0.85, context.profile.preferenceConfidence, 0.001)
        assertEquals(StudyTimeWindow.EVENING, context.profile.bestStudyWindow)
        assertEquals(20.0, context.profile.averageSessionDurationMinutes, 0.001)
        assertEquals(2, context.strengths.size)
        assertEquals(2, context.weaknesses.size)
        assertEquals(1, context.behaviors.size) // 0.20 rate filtered out by default policy (min 0.30)
        assertEquals(2, context.totalCardsTracked)
    }

    @Test
    fun `profile high confidence indicator works correctly`() {
        val highConfProfile = LearningPersonalizationProfile.default().copy(
            primaryStyle = LearningStyleType.PRACTICAL,
            preferenceConfidence = 0.75
        )
        assertTrue(highConfProfile.isHighConfidencePreference)

        val lowConfProfile = LearningPersonalizationProfile.default().copy(
            primaryStyle = LearningStyleType.PRACTICAL,
            preferenceConfidence = 0.40
        )
        assertFalse(lowConfProfile.isHighConfidencePreference)
    }

    // ─────────────────────────────────────────────────────────────────
    // Policy Variant Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `CONCISE_AI policy restricts strengths weaknesses and severe levels`() {
        val memory = sampleMemory()
        val context = LearningPersonalizationBuilder.build(
            memory = memory,
            policy = LearningPersonalizationPolicy.CONCISE_AI,
            nowEpochMs = NOW
        )

        // CONCISE_AI filters weaknesses for MODERATE+ severity (excludes MILD tp_compose)
        assertEquals(1, context.weaknesses.size)
        assertEquals("tp_coroutines", context.weaknesses.first().topicCode)
        assertEquals(WeaknessSeverity.SEVERE, context.weaknesses.first().severity)
    }

    @Test
    fun `REMEDIATION_ONLY policy excludes strengths and behaviors`() {
        val memory = sampleMemory()
        val context = LearningPersonalizationBuilder.build(
            memory = memory,
            policy = LearningPersonalizationPolicy.REMEDIATION_ONLY,
            nowEpochMs = NOW
        )

        assertTrue(context.strengths.isEmpty())
        assertTrue(context.behaviors.isEmpty())
        assertFalse(context.weaknesses.isEmpty())
    }

    @Test
    fun `STRICT_PRIVACY policy excludes all specific topic detail lists and metrics`() {
        val memory = sampleMemory()
        val context = LearningPersonalizationBuilder.build(
            memory = memory,
            policy = LearningPersonalizationPolicy.STRICT_PRIVACY,
            nowEpochMs = NOW
        )

        assertTrue(context.strengths.isEmpty())
        assertTrue(context.weaknesses.isEmpty())
        assertTrue(context.behaviors.isEmpty())
        assertEquals(0.0, context.overallRetentionAverage, 0.001)
        assertEquals(0, context.totalCardsTracked)
        // Profile summary remains intact
        assertEquals(LearningStyleType.PRACTICAL, context.profile.primaryStyle)
    }

    // ─────────────────────────────────────────────────────────────────
    // Prompt Context Generation
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `toPromptContext formats prompt text properly`() {
        val memory = sampleMemory()
        val context = LearningPersonalizationBuilder.build(
            memory = memory,
            activeGoals = listOf("tp_architecture", "tp_testing"),
            nowEpochMs = NOW
        )

        val promptText = context.toPromptContext()
        assertTrue(promptText.contains("=== LEARNER PERSONALIZATION CONTEXT ==="))
        assertTrue(promptText.contains("Learning Style : PRACTICAL (Secondary: VISUAL)"))
        assertTrue(promptText.contains("Study Window   : EVENING"))
        assertTrue(promptText.contains("Kotlin Basics (tp_kotlin)"))
        assertTrue(promptText.contains("Coroutines [SEVERE] (tp_coroutines)"))
        assertTrue(promptText.contains("Active Goals   : tp_architecture, tp_testing"))
    }
}
