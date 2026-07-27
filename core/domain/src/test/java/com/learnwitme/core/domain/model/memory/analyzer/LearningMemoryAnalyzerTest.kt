package com.learnwitme.core.domain.model.memory.analyzer

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.MemoryRecord
import com.learnwitme.core.domain.model.MemoryReview
import com.learnwitme.core.domain.model.MemoryState
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.BehaviorType
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.model.memory.LearningStyleType
import com.learnwitme.core.domain.model.memory.LearningWeakness
import com.learnwitme.core.domain.model.memory.StudyTimeWindow
import com.learnwitme.core.domain.model.memory.WeaknessSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// =====================================================================
// Tests for Sprint 9 Stage 2 — Learning Memory Analyzers
// =====================================================================

class LearningMemoryAnalyzerTest {

    // ─────────────────────────────────────────────────────────────────
    // Test Fixtures
    // ─────────────────────────────────────────────────────────────────

    private val NOW = 1_700_000_000_000L // deterministic "now"
    private val userId = UserId("usr_test")
    private val memId = LearningMemoryId("mem_test")

    private fun cardId(n: Int) = KnowledgeCardId("kc_$n")
    private fun topicCode(t: String) = "tp_$t"
    private fun topicName(t: String) = "Topic $t"

    /** Builds a memory record in MASTERED state with high retention. */
    private fun masteredRecord(cardIndex: Int, topicCode: String): MemoryRecord {
        return MemoryRecord(
            cardId = cardId(cardIndex),
            state = MemoryState.MASTERED,
            retention = 0.92,
            stability = 5.0,
            confidence = 0.9,
            reviews = listOf(
                MemoryReview(timestamp = NOW - 10_000, accuracy = 0.9, hintUsed = false, confidenceRating = 0.9),
                MemoryReview(timestamp = NOW - 5_000, accuracy = 0.95, hintUsed = false, confidenceRating = 0.95)
            )
        )
    }

    /** Builds a memory record in FORGOTTEN state with low retention. */
    private fun forgottenRecord(cardIndex: Int, topicCode: String): MemoryRecord {
        return MemoryRecord(
            cardId = cardId(cardIndex),
            state = MemoryState.FORGOTTEN,
            retention = 0.30,
            stability = 0.5,
            confidence = 0.2,
            reviews = listOf(
                MemoryReview(timestamp = NOW - 3_000, accuracy = 0.2, hintUsed = true, confidenceRating = 0.2),
                MemoryReview(timestamp = NOW - 2_000, accuracy = 0.3, hintUsed = true, confidenceRating = 0.3),
                MemoryReview(timestamp = NOW - 1_000, accuracy = 0.1, hintUsed = true, confidenceRating = 0.1)
            )
        )
    }

    /** Builds a session observation with configurable interactions. */
    private fun session(
        index: Int,
        completed: Boolean = true,
        interactions: List<CardInteraction> = emptyList()
    ): SessionObservation = SessionObservation(
        sessionId = "session_$index",
        startEpochMs = NOW - (index * 86_400_000L), // one session per day ago
        endEpochMs = NOW - (index * 86_400_000L) + 1_800_000L, // 30 min
        completed = completed,
        cardInteractions = interactions
    )

    /** Builds a quiz interaction. */
    private fun quizInteraction(
        cardIndex: Int,
        topic: String,
        accuracy: Double = 0.8,
        hintUsed: Boolean = false,
        wasSkipped: Boolean = false,
        responseMs: Long = 15_000L
    ): CardInteraction = CardInteraction(
        cardId = cardId(cardIndex),
        topicCode = topicCode(topic),
        topicName = topicName(topic),
        isQuiz = true,
        wasSkipped = wasSkipped,
        hintUsed = hintUsed,
        accuracy = accuracy,
        responseTimeMs = responseMs,
        confidenceRating = 0.8,
        readCount = 0,
        interactionEpochMs = NOW - 1000
    )

    /** Builds a read interaction. */
    private fun readInteraction(
        cardIndex: Int,
        topic: String,
        readCount: Int = 1
    ): CardInteraction = CardInteraction(
        cardId = cardId(cardIndex),
        topicCode = topicCode(topic),
        topicName = topicName(topic),
        isQuiz = false,
        wasSkipped = false,
        hintUsed = false,
        accuracy = 0.0,
        responseTimeMs = 0,
        confidenceRating = 0.7,
        readCount = readCount,
        interactionEpochMs = NOW - 2000
    )

    private fun emptyMemory() = LearningMemory(
        id = memId,
        userId = userId
    )

    // ─────────────────────────────────────────────────────────────────
    // LearningWeaknessAnalyzer Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `weakness analyzer detects forgotten topic as MODERATE weakness`() {
        val records = (1..3).map { forgottenRecord(it, topicCode("networking")) }
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("networking") },
            cardToTopicNameMapper = { topicName("networking") },
            nowEpochMs = NOW
        )
        val result = LearningWeaknessAnalyzer.analyze(input, emptyList())
        assertTrue("Expected networking to be a weakness", result.any { it.topicCode == topicCode("networking") })
        val weakness = result.first { it.topicCode == topicCode("networking") }
        assertTrue(weakness.severity == WeaknessSeverity.MODERATE || weakness.severity == WeaknessSeverity.SEVERE)
    }

    @Test
    fun `weakness analyzer classifies SEVERE for consecutive failures`() {
        val severity = LearningWeaknessAnalyzer.classifySeverity(
            averageRetention = 0.3,
            failureRate = 0.5,
            maxConsecutiveFailures = 4
        )
        assertEquals(WeaknessSeverity.SEVERE, severity)
    }

    @Test
    fun `weakness analyzer consecutive failures computed correctly`() {
        val reviews = listOf(
            MemoryReview(NOW - 5000, accuracy = 0.2, hintUsed = true, confidenceRating = 0.2),
            MemoryReview(NOW - 4000, accuracy = 0.3, hintUsed = true, confidenceRating = 0.3),
            MemoryReview(NOW - 3000, accuracy = 0.9, hintUsed = false, confidenceRating = 0.9), // break
            MemoryReview(NOW - 2000, accuracy = 0.1, hintUsed = true, confidenceRating = 0.1),
            MemoryReview(NOW - 1000, accuracy = 0.2, hintUsed = true, confidenceRating = 0.2),
            MemoryReview(NOW, accuracy = 0.1, hintUsed = true, confidenceRating = 0.1)
        )
        val maxStreak = LearningWeaknessAnalyzer.computeConsecutiveFailures(reviews)
        assertEquals(3, maxStreak) // last 3 are consecutive failures
    }

    // ─────────────────────────────────────────────────────────────────
    // LearningStrengthAnalyzer Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `strength analyzer detects mastered topic as strength`() {
        val records = (1..3).map { masteredRecord(it, topicCode("kotlin")) }
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("kotlin") },
            cardToTopicNameMapper = { topicName("kotlin") },
            nowEpochMs = NOW
        )
        val result = LearningStrengthAnalyzer.analyze(input, emptyList())
        assertTrue("Expected kotlin to be a strength", result.any { it.topicCode == topicCode("kotlin") })
        val strength = result.first { it.topicCode == topicCode("kotlin") }
        assertTrue(strength.masteryRate >= 0.80)
        assertTrue(strength.averageRetention >= 0.85)
    }

    @Test
    fun `strength analyzer returns empty for insufficient data`() {
        val records = listOf(masteredRecord(1, topicCode("docker"))) // only 1 card, below MIN
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("docker") },
            cardToTopicNameMapper = { topicName("docker") },
            nowEpochMs = NOW
        )
        val result = LearningStrengthAnalyzer.analyze(input, emptyList())
        assertTrue("Should not qualify with < MIN_CARDS_PER_TOPIC", result.isEmpty())
    }

    @Test
    fun `strength analyzer preserves detection timestamp for existing strength`() {
        val originalDetected = NOW - 1_000_000L
        val existingStrength = com.learnwitme.core.domain.model.memory.LearningStrength(
            topicCode = topicCode("kotlin"),
            topicName = topicName("kotlin"),
            averageRetention = 0.90,
            masteryRate = 0.85,
            masteredCardCount = 3,
            totalCardCount = 3,
            detectedEpochMs = originalDetected
        )
        val records = (1..3).map { masteredRecord(it, topicCode("kotlin")) }
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("kotlin") },
            cardToTopicNameMapper = { topicName("kotlin") },
            nowEpochMs = NOW
        )
        val result = LearningStrengthAnalyzer.analyze(input, listOf(existingStrength))
        val strength = result.firstOrNull { it.topicCode == topicCode("kotlin") }
        assertEquals(originalDetected, strength?.detectedEpochMs)
        assertEquals(NOW, strength?.confirmedEpochMs)
    }

    // ─────────────────────────────────────────────────────────────────
    // LearningPatternAnalyzer Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `pattern analyzer returns empty pattern for no sessions`() {
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = emptyList(),
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningPatternAnalyzer.analyze(input)
        assertEquals(0, result.sessionCount)
        assertFalse(result.isReliable)
    }

    @Test
    fun `pattern analyzer computes completion rate correctly`() {
        val sessions = listOf(
            session(1, completed = true),
            session(2, completed = true),
            session(3, completed = false),
            session(4, completed = true)
        )
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningPatternAnalyzer.analyze(input)
        assertEquals(0.75, result.completionRate, 0.001)
        assertEquals(4, result.sessionCount)
    }

    @Test
    fun `epoch to study window maps correctly`() {
        // 08:00 UTC = MORNING
        val morningEpochMs = 8L * 3600 * 1000
        assertEquals(StudyTimeWindow.MORNING, LearningPatternAnalyzer.epochMsToStudyWindow(morningEpochMs))

        // 21:00 UTC = EVENING
        val eveningEpochMs = 21L * 3600 * 1000
        assertEquals(StudyTimeWindow.EVENING, LearningPatternAnalyzer.epochMsToStudyWindow(eveningEpochMs))
    }

    // ─────────────────────────────────────────────────────────────────
    // LearningPreferenceAnalyzer Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `preference analyzer returns UNDETERMINED for insufficient sessions`() {
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = listOf(session(1), session(2)),  // < MIN (3)
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val existing = LearningPreference.undetermined(NOW)
        val result = LearningPreferenceAnalyzer.analyze(input, existing)
        assertFalse(result.isDetermined)
    }

    @Test
    fun `preference analyzer accumulates observation count`() {
        val existingObservationCount = 10
        val existing = LearningPreference(
            primaryStyle = LearningStyleType.PRACTICAL,
            confidenceScore = 0.6,
            observationCount = existingObservationCount,
            lastUpdatedEpochMs = NOW - 1000
        )
        val sessions = (1..5).map { i ->
            session(i, interactions = listOf(
                quizInteraction(1, "kotlin"),
                quizInteraction(2, "kotlin"),
                readInteraction(3, "kotlin")
            ))
        }
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { topicCode("kotlin") },
            cardToTopicNameMapper = { topicName("kotlin") },
            nowEpochMs = NOW
        )
        val result = LearningPreferenceAnalyzer.analyze(input, existing)
        // If determined: observationCount >= existingObservationCount + 5
        if (result.isDetermined) {
            assertTrue(result.observationCount >= existingObservationCount + sessions.size)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // LearningBehaviorAnalyzer Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `behavior analyzer detects HIGH_HINT_USAGE when hints used heavily`() {
        val sessions = (1..4).map { i ->
            session(i, interactions = listOf(
                quizInteraction(1, "kotlin", hintUsed = true),
                quizInteraction(2, "kotlin", hintUsed = true),
                quizInteraction(3, "kotlin", hintUsed = false)
            ))
        }
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { topicCode("kotlin") },
            cardToTopicNameMapper = { topicName("kotlin") },
            nowEpochMs = NOW
        )
        val result = LearningBehaviorAnalyzer.analyze(input, emptyList())
        val highHint = result.find { it.type == BehaviorType.HIGH_HINT_USAGE }
        assertTrue("Expected HIGH_HINT_USAGE to be detected", highHint != null)
        assertTrue(highHint!!.occurrenceRate >= 0.50)
    }

    @Test
    fun `behavior analyzer detects FREQUENTLY_ABANDONS_SESSIONS`() {
        val sessions = listOf(
            session(1, completed = false),
            session(2, completed = false),
            session(3, completed = true),
            session(4, completed = false)
        )
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningBehaviorAnalyzer.analyze(input, emptyList())
        val abandon = result.find { it.type == BehaviorType.FREQUENTLY_ABANDONS_SESSIONS }
        assertTrue("Expected FREQUENTLY_ABANDONS_SESSIONS", abandon != null)
    }

    @Test
    fun `behavior analyzer detects RAPID_QUIZ_RESPONSES for fast answerers`() {
        val sessions = (1..3).map { i ->
            session(i, interactions = listOf(
                quizInteraction(1, "kotlin", responseMs = 1_000L),
                quizInteraction(2, "kotlin", responseMs = 2_000L),
                quizInteraction(3, "kotlin", responseMs = 3_000L)
            ))
        }
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { topicCode("kotlin") },
            cardToTopicNameMapper = { topicName("kotlin") },
            nowEpochMs = NOW
        )
        val result = LearningBehaviorAnalyzer.analyze(input, emptyList())
        val rapid = result.find { it.type == BehaviorType.RAPID_QUIZ_RESPONSES }
        assertTrue("Expected RAPID_QUIZ_RESPONSES", rapid != null)
    }

    @Test
    fun `behavior analyzer returns empty for insufficient sessions`() {
        val sessions = listOf(session(1))
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = sessions,
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningBehaviorAnalyzer.analyze(input, emptyList())
        assertTrue(result.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────
    // LearningMemoryAnalyzer (Orchestrator) Tests
    // ─────────────────────────────────────────────────────────────────

    @Test
    fun `orchestrator produces correct result structure for empty input`() {
        val memory = emptyMemory()
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = emptyList(),
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningMemoryAnalyzer.analyze(memory, input)
        assertEquals(NOW, result.analyzedEpochMs)
        assertEquals(0, result.totalSessionsAnalyzed)
        assertEquals(0, result.totalCardsAnalyzed)
        assertEquals(0.0, result.overallRetentionAverage, 0.001)
        assertTrue(result.newStrengths.isEmpty())
        assertTrue(result.newWeaknesses.isEmpty())
    }

    @Test
    fun `orchestrator reconciles resolved weaknesses correctly`() {
        // Memory has a weakness for "networking" but current records show recovery
        val existingWeakness = LearningWeakness(
            topicCode = topicCode("networking"),
            topicName = topicName("networking"),
            averageRetention = 0.40,
            failureRate = 0.50,
            forgottenCardCount = 2,
            totalCardCount = 3,
            consecutiveFailureCount = 2,
            severity = WeaknessSeverity.MODERATE,
            detectedEpochMs = NOW - 1_000_000
        )
        // Now all records are high-retention — recovered
        val records = (1..3).map { masteredRecord(it, topicCode("networking")) }
        val memory = LearningMemory(
            id = memId,
            userId = userId,
            records = records,
            weaknesses = listOf(existingWeakness)
        )
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("networking") },
            cardToTopicNameMapper = { topicName("networking") },
            nowEpochMs = NOW
        )
        val result = LearningMemoryAnalyzer.analyze(memory, input)
        // Networking should now be resolved (no longer a weakness)
        assertTrue(
            "Expected networking to be resolved",
            result.resolvedWeaknesses.contains(topicCode("networking"))
        )
        assertFalse(
            "Networking should not still be in weaknesses",
            result.newWeaknesses.any { it.topicCode == topicCode("networking") }
        )
    }

    @Test
    fun `orchestrator result invariant no topic is both strength and weakness`() {
        val records = (1..3).map { masteredRecord(it, topicCode("docker")) }
        val memory = emptyMemory().copy(records = records)
        val input = MemoryAnalysisInput(
            memoryRecords = records,
            recentSessions = emptyList(),
            cardToTopicMapper = { topicCode("docker") },
            cardToTopicNameMapper = { topicName("docker") },
            nowEpochMs = NOW
        )
        val result = LearningMemoryAnalyzer.analyze(memory, input)
        val strengthCodes = result.newStrengths.map { it.topicCode }.toSet()
        val weaknessCodes = result.newWeaknesses.map { it.topicCode }.toSet()
        val overlap = strengthCodes.intersect(weaknessCodes)
        assertTrue("No topic should be both strength and weakness: $overlap", overlap.isEmpty())
    }

    @Test
    fun `analysis result toSummary returns non-blank string`() {
        val memory = emptyMemory()
        val input = MemoryAnalysisInput(
            memoryRecords = emptyList(),
            recentSessions = emptyList(),
            cardToTopicMapper = { "" },
            cardToTopicNameMapper = { "" },
            nowEpochMs = NOW
        )
        val result = LearningMemoryAnalyzer.analyze(memory, input)
        assertTrue(result.toSummary().isNotBlank())
    }
}
