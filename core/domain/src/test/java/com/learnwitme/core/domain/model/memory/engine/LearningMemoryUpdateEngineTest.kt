package com.learnwitme.core.domain.model.memory.engine

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
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
import com.learnwitme.core.domain.model.memory.analyzer.LearningMemoryAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningMemoryUpdateEngineTest {

    // ─────────────────────────────────────────────────────────────────
    // Fixtures
    // ─────────────────────────────────────────────────────────────────

    private val NOW = 1_700_000_000_000L
    private val userId = UserId("usr_test")
    private val memId = LearningMemoryId("mem_test")

    private fun emptyMemory() = LearningMemory(id = memId, userId = userId)

    private fun undeterminedPreference() = LearningPreference.undetermined(nowEpochMs = NOW)

    private fun determinedPreference(
        style: LearningStyleType = LearningStyleType.VISUAL,
        confidence: Double = 0.70,
        observations: Int = 10
    ) = LearningPreference(
        primaryStyle = style,
        confidenceScore = confidence,
        observationCount = observations,
        lastUpdatedEpochMs = NOW - 1000
    )

    private fun emptyPattern() = LearningPattern.empty(nowEpochMs = NOW)

    private fun activePattern(sessions: Int = 8) = LearningPattern(
        bestStudyWindow = StudyTimeWindow.MORNING,
        averageSessionDurationMs = 1_800_000L,
        completionRate = 0.85,
        preferredDifficulty = PatternDifficultyLevel.INTERMEDIATE,
        sessionCount = sessions,
        lastComputedEpochMs = NOW
    )

    private fun strength(topic: String, mastery: Double = 0.90, retention: Double = 0.90) =
        LearningStrength(
            topicCode = "tp_$topic",
            topicName = "Topic $topic",
            averageRetention = retention,
            masteryRate = mastery,
            masteredCardCount = 4,
            totalCardCount = 5,
            detectedEpochMs = NOW - 5000
        )

    private fun weakness(
        topic: String,
        severity: WeaknessSeverity = WeaknessSeverity.MODERATE,
        retention: Double = 0.45
    ) = LearningWeakness(
        topicCode = "tp_$topic",
        topicName = "Topic $topic",
        averageRetention = retention,
        failureRate = 0.35,
        forgottenCardCount = 2,
        totalCardCount = 5,
        consecutiveFailureCount = 2,
        severity = severity,
        detectedEpochMs = NOW - 5000
    )

    private fun behavior(type: BehaviorType, rate: Double = 0.6) = LearningBehavior(
        type = type,
        occurrenceCount = 5,
        occurrenceRate = rate,
        firstObservedEpochMs = NOW - 10_000,
        lastObservedEpochMs = NOW
    )

    private fun emptyAnalysisResult(
        preference: LearningPreference = undeterminedPreference(),
        pattern: LearningPattern = emptyPattern(),
        newStrengths: List<LearningStrength> = emptyList(),
        newWeaknesses: List<LearningWeakness> = emptyList(),
        resolvedWeaknesses: List<String> = emptyList(),
        regressedStrengths: List<String> = emptyList(),
        detectedBehaviors: List<LearningBehavior> = emptyList(),
        sessions: Int = 0
    ) = LearningMemoryAnalysisResult(
        preference = preference,
        pattern = pattern,
        newStrengths = newStrengths,
        newWeaknesses = newWeaknesses,
        resolvedWeaknesses = resolvedWeaknesses,
        regressedStrengths = regressedStrengths,
        detectedBehaviors = detectedBehaviors,
        analyzedEpochMs = NOW,
        totalSessionsAnalyzed = sessions,
        totalCardsAnalyzed = 0,
        overallRetentionAverage = 0.0
    )

    // ─────────────────────────────────────────────────────────────────
    // Preference Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `REPLACE strategy always updates preference`() {
        val memory = emptyMemory()
        val newPref = determinedPreference()
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 5),
            policy = LearningMemoryUpdatePolicy(
                preferenceStrategy = UpdateMergeStrategy.REPLACE,
                minSessionsForPreferenceUpdate = 100 // threshold ignored for REPLACE
            )
        )
        assertEquals(newPref.primaryStyle, result.updatedMemory.preference.primaryStyle)
        assertTrue(result.hasPreferenceChange)
    }

    @Test
    fun `MERGE strategy skips update when sessions below minimum`() {
        val memory = emptyMemory()
        val newPref = determinedPreference()
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 1),
            policy = LearningMemoryUpdatePolicy(
                preferenceStrategy = UpdateMergeStrategy.MERGE,
                minSessionsForPreferenceUpdate = 3
            )
        )
        // Should not update — sessions=1 < min=3
        assertFalse(result.hasPreferenceChange)
        val noChangeUpdate = result.appliedUpdates.firstOrNull {
            it.type == LearningMemoryUpdateType.NO_CHANGE
        }
        assertNotNull(noChangeUpdate)
    }

    @Test
    fun `MERGE strategy skips update when confidence delta below threshold`() {
        val existing = determinedPreference(style = LearningStyleType.VISUAL, confidence = 0.70)
        val memory = emptyMemory().copy(preference = existing)
        val newPref = determinedPreference(style = LearningStyleType.VISUAL, confidence = 0.72) // delta = 0.02
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 5),
            policy = LearningMemoryUpdatePolicy(
                preferenceStrategy = UpdateMergeStrategy.MERGE,
                minSessionsForPreferenceUpdate = 3,
                minConfidenceDeltaForPreferenceChange = 0.05
            )
        )
        // delta 0.02 < threshold 0.05, style same → no change
        assertFalse(result.hasPreferenceChange)
    }

    @Test
    fun `MERGE strategy updates when style changes even with small confidence delta`() {
        val existing = determinedPreference(style = LearningStyleType.VISUAL, confidence = 0.70)
        val memory = emptyMemory().copy(preference = existing)
        val newPref = determinedPreference(style = LearningStyleType.PRACTICAL, confidence = 0.72)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 5),
            policy = LearningMemoryUpdatePolicy(
                preferenceStrategy = UpdateMergeStrategy.MERGE,
                minSessionsForPreferenceUpdate = 3,
                minConfidenceDeltaForPreferenceChange = 0.05
            )
        )
        assertTrue(result.hasPreferenceChange)
        assertEquals(
            LearningStyleType.PRACTICAL,
            result.updatedMemory.preference.primaryStyle
        )
    }

    @Test
    fun `MERGE strategy preserves higher observation count from existing`() {
        val existing = determinedPreference(style = LearningStyleType.VISUAL, observations = 50)
        val memory = emptyMemory().copy(preference = existing)
        val newPref = determinedPreference(
            style = LearningStyleType.PRACTICAL,
            confidence = 0.80,
            observations = 5
        )
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 5),
            policy = LearningMemoryUpdatePolicy(
                preferenceStrategy = UpdateMergeStrategy.MERGE,
                minSessionsForPreferenceUpdate = 3
            )
        )
        // Should keep the max observation count (50)
        assertEquals(50, result.updatedMemory.preference.observationCount)
    }

    @Test
    fun `IGNORE strategy never updates preference`() {
        val memory = emptyMemory()
        val newPref = determinedPreference()
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(preference = newPref, sessions = 10),
            policy = LearningMemoryUpdatePolicy(preferenceStrategy = UpdateMergeStrategy.IGNORE)
        )
        assertFalse(result.hasPreferenceChange)
        assertEquals(LearningStyleType.UNDETERMINED, result.updatedMemory.preference.primaryStyle)
    }

    // ─────────────────────────────────────────────────────────────────
    // Pattern Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `REPLACE pattern strategy always replaces existing pattern`() {
        val existing = activePattern(sessions = 20)
        val memory = emptyMemory().copy(pattern = existing)
        val newPattern = activePattern(sessions = 3)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(pattern = newPattern),
            policy = LearningMemoryUpdatePolicy(patternStrategy = UpdateMergeStrategy.REPLACE)
        )
        assertEquals(3, result.updatedMemory.pattern.sessionCount)
        assertTrue(result.hasPatternChange)
    }

    @Test
    fun `MERGE pattern strategy accumulates session counts`() {
        val existing = activePattern(sessions = 10)
        val memory = emptyMemory().copy(pattern = existing)
        val newPattern = activePattern(sessions = 5)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(pattern = newPattern),
            policy = LearningMemoryUpdatePolicy(patternStrategy = UpdateMergeStrategy.MERGE)
        )
        assertEquals(15, result.updatedMemory.pattern.sessionCount)
    }

    // ─────────────────────────────────────────────────────────────────
    // Strength Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `new strength is added to aggregate`() {
        val memory = emptyMemory()
        val s = strength("kotlin")
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(newStrengths = listOf(s))
        )
        assertTrue(result.updatedMemory.strengths.any { it.topicCode == "tp_kotlin" })
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.STRENGTH_ADDED })
    }

    @Test
    fun `existing strength is confirmed not added again`() {
        val s = strength("kotlin")
        val memory = emptyMemory().copy(strengths = listOf(s))
        val refreshed = s.copy(confirmedEpochMs = NOW)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(newStrengths = listOf(refreshed))
        )
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.STRENGTH_CONFIRMED })
        assertFalse(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.STRENGTH_ADDED })
    }

    @Test
    fun `regressed strength is removed from aggregate`() {
        val s = strength("docker")
        val memory = emptyMemory().copy(strengths = listOf(s))
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(regressedStrengths = listOf("tp_docker"))
        )
        assertFalse(result.updatedMemory.strengths.any { it.topicCode == "tp_docker" })
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.STRENGTH_REMOVED })
    }

    @Test
    fun `strength cap prunes lowest mastery entries`() {
        val strengths = listOf(
            strength("a", mastery = 0.99),
            strength("b", mastery = 0.82),
            strength("c", mastery = 0.95)
        )
        val memory = emptyMemory()
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(newStrengths = strengths),
            policy = LearningMemoryUpdatePolicy(maxStrengthsRetained = 2)
        )
        assertEquals(2, result.updatedMemory.strengths.size)
        // Should keep the two highest mastery: "a" (0.99) and "c" (0.95)
        val codes = result.updatedMemory.strengths.map { it.topicCode }.toSet()
        assertTrue("tp_a" in codes)
        assertTrue("tp_c" in codes)
        assertFalse("tp_b" in codes)
    }

    // ─────────────────────────────────────────────────────────────────
    // Weakness Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `new weakness is added to aggregate`() {
        val memory = emptyMemory()
        val w = weakness("networking")
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(newWeaknesses = listOf(w))
        )
        assertTrue(result.updatedMemory.weaknesses.any { it.topicCode == "tp_networking" })
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.WEAKNESS_ADDED })
    }

    @Test
    fun `worsened weakness is classified correctly`() {
        val existing = weakness("networking", severity = WeaknessSeverity.MILD)
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val worsened = weakness("networking", severity = WeaknessSeverity.SEVERE)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(newWeaknesses = listOf(worsened)),
            policy = LearningMemoryUpdatePolicy(
                weaknessSeverityEscalationThreshold = WeaknessSeverity.MODERATE
            )
        )
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.WEAKNESS_WORSENED })
    }

    @Test
    fun `resolved weakness is removed when policy allows`() {
        val existing = weakness("concurrency")
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(resolvedWeaknesses = listOf("tp_concurrency")),
            policy = LearningMemoryUpdatePolicy(removeResolvedWeaknesses = true)
        )
        assertFalse(result.updatedMemory.weaknesses.any { it.topicCode == "tp_concurrency" })
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.WEAKNESS_RESOLVED })
    }

    @Test
    fun `resolved weakness is retained when policy forbids removal`() {
        val existing = weakness("concurrency")
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(resolvedWeaknesses = listOf("tp_concurrency")),
            policy = LearningMemoryUpdatePolicy(removeResolvedWeaknesses = false)
        )
        assertTrue(result.updatedMemory.weaknesses.any { it.topicCode == "tp_concurrency" })
        assertFalse(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.WEAKNESS_RESOLVED })
    }

    // ─────────────────────────────────────────────────────────────────
    // Behavior Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `new behavior is added to aggregate`() {
        val memory = emptyMemory()
        val b = behavior(BehaviorType.HIGH_HINT_USAGE)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(detectedBehaviors = listOf(b))
        )
        assertTrue(result.updatedMemory.behaviors.any { it.type == BehaviorType.HIGH_HINT_USAGE })
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.BEHAVIOR_ADDED })
    }

    @Test
    fun `existing behavior is updated not added again`() {
        val b = behavior(BehaviorType.SLOW_QUIZ_RESPONSES)
        val memory = emptyMemory().copy(behaviors = listOf(b))
        val refreshed = b.copy(occurrenceCount = 10, lastObservedEpochMs = NOW)
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(detectedBehaviors = listOf(refreshed))
        )
        assertTrue(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.BEHAVIOR_UPDATED })
        assertFalse(result.appliedUpdates.any { it.type == LearningMemoryUpdateType.BEHAVIOR_ADDED })
    }

    @Test
    fun `non-redetected behavior is removed when policy preserveBehaviors is false`() {
        val existing = behavior(BehaviorType.FREQUENTLY_SKIPS_QUIZZES)
        val memory = emptyMemory().copy(behaviors = listOf(existing))
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            // No behaviors detected this cycle
            analysisResult = emptyAnalysisResult(detectedBehaviors = emptyList()),
            policy = LearningMemoryUpdatePolicy(preserveBehaviorsIfNotRedetected = false)
        )
        assertFalse(result.updatedMemory.behaviors.any { it.type == BehaviorType.FREQUENTLY_SKIPS_QUIZZES })
    }

    @Test
    fun `non-redetected behavior is preserved when policy preserveBehaviors is true`() {
        val existing = behavior(BehaviorType.FREQUENTLY_SKIPS_QUIZZES)
        val memory = emptyMemory().copy(behaviors = listOf(existing))
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(detectedBehaviors = emptyList()),
            policy = LearningMemoryUpdatePolicy(preserveBehaviorsIfNotRedetected = true)
        )
        assertTrue(result.updatedMemory.behaviors.any { it.type == BehaviorType.FREQUENTLY_SKIPS_QUIZZES })
    }

    // ─────────────────────────────────────────────────────────────────
    // Mutual Exclusion & Ordering Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `weakness resolved before strength added for same topic`() {
        // Topic "kotlin" is a weakness; analysis says it recovered AND is now a strength
        val existingWeakness = weakness("kotlin")
        val memory = emptyMemory().copy(weaknesses = listOf(existingWeakness))
        val newStrength = strength("kotlin")
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(
                newStrengths = listOf(newStrength),
                resolvedWeaknesses = listOf("tp_kotlin")
            )
        )
        // Weakness resolved, strength added — no mutual-exclusion violation
        assertFalse(result.updatedMemory.weaknesses.any { it.topicCode == "tp_kotlin" })
        assertTrue(result.updatedMemory.strengths.any { it.topicCode == "tp_kotlin" })
    }

    @Test
    fun `aggregate invariant holds after full update cycle`() {
        val memory = emptyMemory()
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = memory,
            analysisResult = emptyAnalysisResult(
                newStrengths = listOf(strength("a"), strength("b")),
                newWeaknesses = listOf(weakness("c"), weakness("d"))
            )
        )
        // No topic in both lists
        val sCodes = result.updatedMemory.strengths.map { it.topicCode }.toSet()
        val wCodes = result.updatedMemory.weaknesses.map { it.topicCode }.toSet()
        assertTrue(sCodes.intersect(wCodes).isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────
    // Engine Result Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `engine result has correct epoch timestamp`() {
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = emptyMemory(),
            analysisResult = emptyAnalysisResult()
        )
        assertEquals(NOW, result.computedEpochMs)
    }

    @Test
    fun `engine result toSummary returns non-blank string`() {
        val result = LearningMemoryUpdateEngine.apply(
            currentMemory = emptyMemory(),
            analysisResult = emptyAnalysisResult(
                newStrengths = listOf(strength("kotlin"))
            )
        )
        assertTrue(result.toSummary().isNotBlank())
    }

    @Test
    fun `LearningMemoryUpdate factory methods produce correct types`() {
        val pref = determinedPreference()
        val prefUpdate = LearningMemoryUpdate.preferenceUpdated(pref, true, "test", NOW)
        assertEquals(LearningMemoryUpdateType.PREFERENCE_DETERMINED, prefUpdate.type)
        assertTrue(prefUpdate.isMeaningfulChange)

        val noChange = LearningMemoryUpdate.noChange("nothing changed", NOW)
        assertEquals(LearningMemoryUpdateType.NO_CHANGE, noChange.type)
        assertFalse(noChange.isMeaningfulChange)

        val strengthRemoved = LearningMemoryUpdate.strengthRemoved("tp_test", NOW)
        assertTrue(strengthRemoved.affectsStrength)
        assertFalse(strengthRemoved.affectsWeakness)
    }

    @Test
    fun `policy invariants are enforced`() {
        try {
            LearningMemoryUpdatePolicy(minConfidenceDeltaForPreferenceChange = 1.5)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("minConfidenceDeltaForPreferenceChange"))
        }
        try {
            LearningMemoryUpdatePolicy(minSessionsForPreferenceUpdate = -1)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("minSessionsForPreferenceUpdate"))
        }
    }
}
