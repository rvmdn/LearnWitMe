package com.learnwitme.core.domain.model.memory.event

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
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryEngineResult
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdate
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdateEngine
import com.learnwitme.core.domain.model.memory.engine.LearningMemoryUpdatePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningMemoryEventFactoryTest {

    // ─────────────────────────────────────────────────────────────────
    // Fixtures
    // ─────────────────────────────────────────────────────────────────

    private val NOW = 1_700_000_000_000L
    private val userId = UserId("usr_test")
    private val memId = LearningMemoryId("mem_test")

    // Deterministic ID generator — predictable for test assertions
    private var idCounter = 0
    private val factory = LearningMemoryEventFactory { seed -> "evt_${++idCounter}_$seed" }

    private fun emptyMemory() = LearningMemory(id = memId, userId = userId)

    private fun strength(topic: String) = LearningStrength(
        topicCode = "tp_$topic",
        topicName = "Topic $topic",
        averageRetention = 0.92,
        masteryRate = 0.90,
        masteredCardCount = 4,
        totalCardCount = 5,
        detectedEpochMs = NOW - 5000
    )

    private fun weakness(topic: String, severity: WeaknessSeverity = WeaknessSeverity.SEVERE) =
        LearningWeakness(
            topicCode = "tp_$topic",
            topicName = "Topic $topic",
            averageRetention = 0.30,
            failureRate = 0.50,
            forgottenCardCount = 3,
            totalCardCount = 5,
            consecutiveFailureCount = 4,
            severity = severity,
            detectedEpochMs = NOW - 100_000
        )

    private fun determinedPreference() = LearningPreference(
        primaryStyle = LearningStyleType.PRACTICAL,
        confidenceScore = 0.75,
        observationCount = 10,
        lastUpdatedEpochMs = NOW
    )

    private fun analysisResult(
        preference: LearningPreference = LearningPreference.undetermined(NOW),
        newStrengths: List<LearningStrength> = emptyList(),
        newWeaknesses: List<LearningWeakness> = emptyList(),
        resolvedWeaknesses: List<String> = emptyList(),
        regressedStrengths: List<String> = emptyList(),
        sessions: Int = 5
    ) = LearningMemoryAnalysisResult(
        preference = preference,
        pattern = LearningPattern.empty(NOW),
        newStrengths = newStrengths,
        newWeaknesses = newWeaknesses,
        resolvedWeaknesses = resolvedWeaknesses,
        regressedStrengths = regressedStrengths,
        detectedBehaviors = emptyList(),
        analyzedEpochMs = NOW,
        totalSessionsAnalyzed = sessions,
        totalCardsAnalyzed = 0,
        overallRetentionAverage = 0.0
    )

    private fun runEngine(
        currentMemory: LearningMemory,
        result: LearningMemoryAnalysisResult,
        policy: LearningMemoryUpdatePolicy = LearningMemoryUpdatePolicy.DEFAULT
    ) = LearningMemoryUpdateEngine.apply(currentMemory, result, policy)

    // ─────────────────────────────────────────────────────────────────
    // LearningMemoryUpdatedEvent — always produced
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `fromEngineResult always produces LearningMemoryUpdatedEvent`() {
        val memory = emptyMemory()
        val engineResult = runEngine(memory, analysisResult())
        val events = factory.fromEngineResult(engineResult, memory)
        assertTrue(events.isNotEmpty())
        assertTrue(events.first() is LearningMemoryUpdatedEvent)
    }

    @Test
    fun `LearningMemoryUpdatedEvent carries correct metadata`() {
        val memory = emptyMemory()
        val engineResult = runEngine(memory, analysisResult())
        val events = factory.fromEngineResult(engineResult, memory)
        val event = events.first() as LearningMemoryUpdatedEvent
        assertEquals(LearningMemoryEventType.MEMORY_UPDATED, event.type)
        assertEquals(memId, event.memoryId)
        assertEquals(userId, event.userId)
        assertEquals(NOW, event.occurredAtMs)
        assertTrue(event.eventId.isNotBlank())
    }

    @Test
    fun `LearningMemoryUpdatedEvent previousSnapshot reflects pre-update state`() {
        val existingStrength = strength("kotlin")
        val memory = emptyMemory().copy(strengths = listOf(existingStrength))
        val engineResult = runEngine(memory, analysisResult(regressedStrengths = listOf("tp_kotlin")))
        val events = factory.fromEngineResult(engineResult, memory)
        val event = events.first() as LearningMemoryUpdatedEvent
        // previousSnapshot should reflect 1 strength (before removal)
        assertEquals(1, event.previousSnapshot.strengthCount)
        // currentSnapshot should reflect 0 strengths (after removal)
        assertEquals(0, event.currentSnapshot.strengthCount)
    }

    // ─────────────────────────────────────────────────────────────────
    // PreferenceDetermined event
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `PreferenceDetermined event is produced when style transitions from UNDETERMINED`() {
        val memory = emptyMemory() // starts UNDETERMINED
        val result = analysisResult(
            preference = determinedPreference(),
            sessions = 5
        )
        val engineResult = runEngine(
            memory,
            result,
            LearningMemoryUpdatePolicy(
                preferenceStrategy = com.learnwitme.core.domain.model.memory.engine.UpdateMergeStrategy.REPLACE
            )
        )
        val events = factory.fromEngineResult(engineResult, memory)
        val prefEvent = events.filterIsInstance<LearningMemoryPreferenceDeterminedEvent>()
        assertTrue("Expected PreferenceDetermined event", prefEvent.isNotEmpty())
        assertEquals(LearningStyleType.PRACTICAL, prefEvent.first().determinedStyle)
        assertEquals(0.75, prefEvent.first().confidenceScore, 0.001)
    }

    @Test
    fun `PreferenceDetermined event is NOT produced when preference was already determined`() {
        val existing = determinedPreference()
        val memory = emptyMemory().copy(preference = existing)
        val newPref = determinedPreference().copy(
            primaryStyle = LearningStyleType.VISUAL,
            confidenceScore = 0.85
        )
        val result = analysisResult(preference = newPref, sessions = 5)
        val engineResult = runEngine(
            memory,
            result,
            LearningMemoryUpdatePolicy(
                preferenceStrategy = com.learnwitme.core.domain.model.memory.engine.UpdateMergeStrategy.REPLACE
            )
        )
        val events = factory.fromEngineResult(engineResult, memory)
        val prefDetermined = events.filterIsInstance<LearningMemoryPreferenceDeterminedEvent>()
        // Style changed but was already determined — no PreferenceDetermined event
        assertTrue(prefDetermined.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────
    // StrengthDetected event
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `StrengthDetected event is produced for each new strength`() {
        val memory = emptyMemory()
        val result = analysisResult(newStrengths = listOf(strength("kotlin"), strength("docker")))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val strengthEvents = events.filterIsInstance<LearningMemoryStrengthDetectedEvent>()
        assertEquals(2, strengthEvents.size)
        val topics = strengthEvents.map { it.topicCode }.toSet()
        assertTrue("tp_kotlin" in topics)
        assertTrue("tp_docker" in topics)
    }

    @Test
    fun `StrengthDetected event is NOT produced for confirmed strengths`() {
        val s = strength("kotlin")
        val memory = emptyMemory().copy(strengths = listOf(s)) // already tracked
        val result = analysisResult(newStrengths = listOf(s.copy(confirmedEpochMs = NOW)))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val strengthEvents = events.filterIsInstance<LearningMemoryStrengthDetectedEvent>()
        assertTrue("No StrengthDetected for re-confirmed strengths", strengthEvents.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────
    // SevereWeaknessDetected event
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `SevereWeaknessDetected event is produced for SEVERE weakness`() {
        val memory = emptyMemory()
        val severe = weakness("networking", WeaknessSeverity.SEVERE)
        val result = analysisResult(newWeaknesses = listOf(severe))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val severeEvents = events.filterIsInstance<LearningMemorySevereWeaknessDetectedEvent>()
        assertEquals(1, severeEvents.size)
        assertTrue(severeEvents.first().isNewWeakness)
        assertEquals("tp_networking", severeEvents.first().topicCode)
    }

    @Test
    fun `SevereWeaknessDetected event is NOT produced for MODERATE weakness`() {
        val memory = emptyMemory()
        val moderate = weakness("networking", WeaknessSeverity.MODERATE)
        val result = analysisResult(newWeaknesses = listOf(moderate))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val severeEvents = events.filterIsInstance<LearningMemorySevereWeaknessDetectedEvent>()
        assertTrue(severeEvents.isEmpty())
    }

    @Test
    fun `SevereWeaknessDetected marks isNewWeakness=false when existing worsened`() {
        val existing = weakness("networking", WeaknessSeverity.MODERATE)
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val worsened = weakness("networking", WeaknessSeverity.SEVERE)
        val result = analysisResult(newWeaknesses = listOf(worsened))
        val engineResult = runEngine(
            memory,
            result,
            LearningMemoryUpdatePolicy(
                weaknessSeverityEscalationThreshold = WeaknessSeverity.MODERATE
            )
        )
        val events = factory.fromEngineResult(engineResult, memory)
        val severeEvents = events.filterIsInstance<LearningMemorySevereWeaknessDetectedEvent>()
        assertEquals(1, severeEvents.size)
        assertFalse("Worsened weakness is not new", severeEvents.first().isNewWeakness)
    }

    // ─────────────────────────────────────────────────────────────────
    // WeaknessResolved event
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `WeaknessResolved event is produced when weakness is resolved`() {
        val existing = weakness("concurrency")
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val result = analysisResult(resolvedWeaknesses = listOf("tp_concurrency"))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val resolvedEvents = events.filterIsInstance<LearningMemoryWeaknessResolvedEvent>()
        assertEquals(1, resolvedEvents.size)
        assertEquals("tp_concurrency", resolvedEvents.first().topicCode)
        assertEquals("Topic concurrency", resolvedEvents.first().topicName)
    }

    @Test
    fun `WeaknessResolved event computes resolvedAfterMs correctly`() {
        val detectedMs = NOW - 500_000L
        val existing = weakness("concurrency").copy(
            detectedEpochMs = detectedMs
        )
        val memory = emptyMemory().copy(weaknesses = listOf(existing))
        val result = analysisResult(resolvedWeaknesses = listOf("tp_concurrency"))
        val engineResult = runEngine(memory, result)
        val events = factory.fromEngineResult(engineResult, memory)
        val resolved = events.filterIsInstance<LearningMemoryWeaknessResolvedEvent>().first()
        assertEquals(500_000L, resolved.resolvedAfterMs)
    }

    // ─────────────────────────────────────────────────────────────────
    // SnapshotCreated event
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `fromSnapshot produces LearningMemorySnapshotCreatedEvent with correct fields`() {
        val memory = emptyMemory()
        val snapshot = memory.takeSnapshot(
            snapshotId = "snap_001",
            capturedEpochMs = NOW,
            reason = "Weekly digest"
        )
        val event = factory.fromSnapshot(snapshot, memId, userId, NOW)
        assertEquals(LearningMemoryEventType.SNAPSHOT_CREATED, event.type)
        assertEquals("snap_001", event.snapshotId)
        assertEquals("Weekly digest", event.snapshotReason)
        assertEquals(memId, event.memoryId)
        assertEquals(userId, event.userId)
        assertEquals(NOW, event.occurredAtMs)
    }

    // ─────────────────────────────────────────────────────────────────
    // Event ordering contract
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `fromEngineResult produces events in declared ordering`() {
        val memory = emptyMemory()
        val result = analysisResult(
            preference = determinedPreference(),
            newStrengths = listOf(strength("kotlin")),
            newWeaknesses = listOf(weakness("networking", WeaknessSeverity.SEVERE)),
            sessions = 5
        )
        val engineResult = runEngine(
            memory,
            result,
            LearningMemoryUpdatePolicy(
                preferenceStrategy = com.learnwitme.core.domain.model.memory.engine.UpdateMergeStrategy.REPLACE
            )
        )
        val events = factory.fromEngineResult(engineResult, memory)

        assertTrue(events.size >= 4)
        assertTrue(events[0] is LearningMemoryUpdatedEvent)
        // PreferenceDetermined comes before StrengthDetected
        val prefIdx = events.indexOfFirst { it is LearningMemoryPreferenceDeterminedEvent }
        val strIdx = events.indexOfFirst { it is LearningMemoryStrengthDetectedEvent }
        val severeIdx = events.indexOfFirst { it is LearningMemorySevereWeaknessDetectedEvent }
        if (prefIdx >= 0 && strIdx >= 0) assertTrue(prefIdx < strIdx)
        if (strIdx >= 0 && severeIdx >= 0) assertTrue(strIdx < severeIdx)
    }

    // ─────────────────────────────────────────────────────────────────
    // MemoryStateSnapshot tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `MemoryStateSnapshot reflects correct counts from LearningMemory`() {
        val memory = emptyMemory().copy(
            strengths = listOf(strength("kotlin"), strength("docker")),
            weaknesses = listOf(weakness("networking")),
            behaviors = listOf(
                LearningBehavior(
                    type = BehaviorType.HIGH_HINT_USAGE,
                    occurrenceCount = 5,
                    occurrenceRate = 0.6,
                    firstObservedEpochMs = NOW,
                    lastObservedEpochMs = NOW
                )
            )
        )
        val snapshot = memory.toStateSnapshot(NOW)
        assertEquals(2, snapshot.strengthCount)
        assertEquals(1, snapshot.weaknessCount)
        assertEquals(1, snapshot.behaviorCount)
        assertTrue(snapshot.hasStrengths)
        assertTrue(snapshot.hasWeaknesses)
    }

    // ─────────────────────────────────────────────────────────────────
    // Event field invariants
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `WeaknessResolvedEvent resolvedAfterHours computed correctly`() {
        val event = LearningMemoryWeaknessResolvedEvent(
            eventId = "evt_001",
            memoryId = memId,
            userId = userId,
            occurredAtMs = NOW,
            topicCode = "tp_test",
            topicName = "Test Topic",
            resolvedAfterMs = 7_200_000L // 2 hours
        )
        assertEquals(2.0, event.resolvedAfterHours, 0.001)
    }

    @Test
    fun `PreferenceDetermined event rejects UNDETERMINED style`() {
        try {
            LearningMemoryPreferenceDeterminedEvent(
                eventId = "evt_001",
                memoryId = memId,
                userId = userId,
                occurredAtMs = NOW,
                determinedStyle = LearningStyleType.UNDETERMINED,
                confidenceScore = 0.5,
                observationCount = 5
            )
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("UNDETERMINED"))
        }
    }

    @Test
    fun `SevereWeaknessDetected event rejects non-SEVERE weakness`() {
        try {
            LearningMemorySevereWeaknessDetectedEvent(
                eventId = "evt_001",
                memoryId = memId,
                userId = userId,
                occurredAtMs = NOW,
                weakness = weakness("test", WeaknessSeverity.MILD),
                isNewWeakness = true
            )
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("SEVERE"))
        }
    }
}
