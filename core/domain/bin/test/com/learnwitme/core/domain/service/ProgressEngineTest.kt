package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ProgressEngineTest {

    private class FakeClock(var time: Long) : Clock {
        override fun currentTimeMillis(): Long = time
    }

    private val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    private fun createBasicProfile(): UserProfile {
        return UserProfile(
            id = UserId("u1"),
            careerPath = CareerPath("cp_dev", "Developer", ""),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0)
        )
    }

    private fun createRoadmap(totalUnits: Int, completedUnits: Int): LearningRoadmap {
        val goal = LearningGoal("Goal", "cp_dev", DifficultyLevel.MID)
        val units = (1..totalUnits).map {
            LearningUnit(LearningUnitId("u$it"), "Unit $it", KnowledgeCardId("c$it"), null, 10, "tp")
        }
        val completed = (1..completedUnits).map { LearningUnitId("u$it") }.toSet()
        return LearningRoadmap(LearningRoadmapId("r1"), goal, units, RoadmapStatus.ACTIVE, completed)
    }

    @Test
    fun testCompletionCalculation() {
        val engine = ProgressEngine()
        val roadmap = createRoadmap(10, 4) // 40% completion
        val decision = engine.calculateProgress(
            roadmap = roadmap,
            memory = LearningMemory(LearningMemoryId("m1"), UserId("u1")),
            sessions = emptyList(),
            profile = createBasicProfile(),
            clock = FakeClock(1000L),
            policy = ProgressPolicy()
        )
        
        assertEquals(0.4, decision.summary.completionPercentage, 0.001)
        assertEquals(4, decision.updatedProfile.progress.totalUnitsCompleted)
    }

    @Test
    fun testMasteryCalculation() {
        val engine = ProgressEngine()
        // Memory with 2 records, retention 0.8 and 0.4. Avg mastery = 0.6
        val memory = LearningMemory(
            id = LearningMemoryId("m1"),
            userId = UserId("u1"),
            records = listOf(
                MemoryRecord(KnowledgeCardId("c1"), MemoryState.MASTERED, 0.8, 10.0, 0.8),
                MemoryRecord(KnowledgeCardId("c2"), MemoryState.LEARNING, 0.4, 1.0, 0.5)
            )
        )
        val decision = engine.calculateProgress(
            roadmap = createRoadmap(1, 0),
            memory = memory,
            sessions = emptyList(),
            profile = createBasicProfile(),
            clock = FakeClock(1000L),
            policy = ProgressPolicy()
        )
        
        assertEquals(0.6, decision.summary.masteryPercentage, 0.001)
        assertEquals(1, decision.updatedProfile.progress.totalCardsMastered)
    }

    @Test
    fun testLearningStreakMaintainedAndReset() {
        val engine = ProgressEngine()
        val policy = ProgressPolicy()
        val profile = createBasicProfile()
        
        // Day 1: Session 1
        val day1Start = 1 * ONE_DAY_MS
        val s1 = LearningSession(LearningSessionId("s1"), day1Start, day1Start + 1000, emptyList())
        
        // Day 2: Session 2 (Maintains streak -> streak 2)
        val day2Start = 2 * ONE_DAY_MS
        val s2 = LearningSession(LearningSessionId("s2"), day2Start, day2Start + 1000, emptyList())
        
        // Day 5: Session 3 (Missed day 3 and 4. Reset to 1)
        val day5Start = 5 * ONE_DAY_MS
        val s3 = LearningSession(LearningSessionId("s3"), day5Start, day5Start + 1000, emptyList())
        
        // Let's test first with s1 and s2
        val dec1 = engine.calculateProgress(createRoadmap(0, 0), LearningMemory(LearningMemoryId("m1"), UserId("u1")), listOf(s1, s2), profile, FakeClock(day2Start + 2000), policy)
        
        assertEquals(2, dec1.summary.currentStreak)
        
        // Test with s3 to break it. When evaluating s3, last active was day2. Difference is 3 days. Reset to 1.
        val dec2 = engine.calculateProgress(createRoadmap(0, 0), LearningMemory(LearningMemoryId("m1"), UserId("u1")), listOf(s1, s2, s3), profile, FakeClock(day5Start + 2000), policy)
        
        assertEquals(1, dec2.summary.currentStreak)
        
        // Test broken by clock (time passing without session)
        // From dec1 (streak 2 at day 2), jump to day 5 but no new sessions
        val profileAfterDec1 = dec1.updatedProfile
        val dec3 = engine.calculateProgress(createRoadmap(0, 0), LearningMemory(LearningMemoryId("m1"), UserId("u1")), emptyList(), profileAfterDec1, FakeClock(day5Start), policy)
        
        assertEquals(0, dec3.summary.currentStreak)
        assertEquals(0, dec3.updatedProfile.progress.activeStreakDays)
    }

    @Test
    fun testDailyAndWeeklyProgressAggregation() {
        val engine = ProgressEngine()
        val currentTime = 10 * ONE_DAY_MS
        val clock = FakeClock(currentTime)
        
        // Session today (day 10)
        val sToday = LearningSession(LearningSessionId("st"), currentTime - 1000, currentTime, listOf(LearningUnitId("u1"), LearningUnitId("u2")))
        
        // Session yesterday (day 9)
        val sYesterday = LearningSession(LearningSessionId("sy"), currentTime - ONE_DAY_MS - 1000, currentTime - ONE_DAY_MS, listOf(LearningUnitId("u3")))
        
        // Session 8 days ago (day 2)
        val sOld = LearningSession(LearningSessionId("so"), currentTime - 8 * ONE_DAY_MS, currentTime - 8 * ONE_DAY_MS + 1000, listOf(LearningUnitId("u4")))
        
        val sessions = listOf(sToday, sYesterday, sOld)
        
        val decision = engine.calculateProgress(createRoadmap(0, 0), LearningMemory(LearningMemoryId("m1"), UserId("u1")), sessions, createBasicProfile(), clock, ProgressPolicy())
        
        // Daily: u1, u2 = 2
        assertEquals(2, decision.summary.dailyProgressUnits)
        
        // Weekly (last 7 days): u1, u2 (today) + u3 (yesterday) = 3
        assertEquals(3, decision.summary.weeklyProgressUnits)
    }

    @Test
    fun testLearningVelocity() {
        val engine = ProgressEngine()
        val currentTime = 10 * ONE_DAY_MS
        val clock = FakeClock(currentTime)
        
        // Session today, duration 30 minutes, 3 units completed
        val sToday = LearningSession(LearningSessionId("st"), currentTime - 30 * 60 * 1000, currentTime, listOf(LearningUnitId("u1"), LearningUnitId("u2"), LearningUnitId("u3")))
        
        val decision = engine.calculateProgress(createRoadmap(0, 0), LearningMemory(LearningMemoryId("m1"), UserId("u1")), listOf(sToday), createBasicProfile(), clock, ProgressPolicy())
        
        // Velocity = 3 / 30 = 0.1 units per minute
        assertEquals(0.1, decision.summary.learningVelocityUnitsPerMinute, 0.001)
    }
}
