package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class FeedEngineTest {

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

    private fun createMockProgress(): ProgressSummary {
        return ProgressSummary(0.5, 0.5, 1, 1, 5, 0.5)
    }

    private fun createMockRecommendation(
        topId: String?,
        altIds: List<String>
    ): RecommendationDecision {
        val top = topId?.let { 
            RecommendationCandidate(LearningUnit(LearningUnitId(it), "U", KnowledgeCardId(it), null, 10, "t"), 100.0, listOf("r")) 
        }
        val alts = altIds.map { 
            RecommendationCandidate(LearningUnit(LearningUnitId(it), "U", KnowledgeCardId(it), null, 10, "t"), 50.0, listOf("r")) 
        }
        return RecommendationDecision(top, alts)
    }

    @Test
    fun testFeedCompositionAndDuplicateRemoval() {
        val engine = FeedEngine()
        
        // Card "c1" is top recommendation.
        // Card "c2" and "c1" are in memory as LEARNING.
        // Card "c3" and "c2" are in review queue.
        // Card "c4" and "c3" are alternatives.
        
        val recommendation = createMockRecommendation("c1", listOf("c4", "c3"))
        
        val memory = LearningMemory(
            LearningMemoryId("m1"), UserId("u1"),
            listOf(
                MemoryRecord(KnowledgeCardId("c1"), MemoryState.LEARNING, 0.5, 1.0, 0.5),
                MemoryRecord(KnowledgeCardId("c2"), MemoryState.LEARNING, 0.5, 1.0, 0.5)
            )
        )
        
        val reviewQueue = ReviewQueue(
            listOf(
                ReviewDecision(KnowledgeCardId("c3"), 100L, ReviewPriority.CRITICAL, true, false, "overdue"),
                ReviewDecision(KnowledgeCardId("c2"), 200L, ReviewPriority.MEDIUM, false, false, "schedule")
            )
        )
        
        val decision = engine.composeFeed(
            recommendation,
            createMockProgress(),
            reviewQueue,
            KnowledgeDecision(KnowledgeGraph(emptyMap(), emptyList()), LearningPath(emptyList()), emptyMap()),
            memory,
            createBasicProfile(),
            FeedPolicy()
        )
        
        val sections = decision.feed.sections
        assertEquals(6, sections.size)
        
        // 1. Focus
        assertEquals(FeedSectionType.TODAYS_FOCUS, sections[0].type)
        assertEquals(1, sections[0].items.size)
        assertEquals("c1", sections[0].items[0].cardId.value)
        
        // 2. Continue Learning ("c1" is removed as duplicate, only "c2" remains)
        assertEquals(FeedSectionType.CONTINUE_LEARNING, sections[1].type)
        assertEquals(1, sections[1].items.size)
        assertEquals("c2", sections[1].items[0].cardId.value)
        
        // 3. Review Today ("c2" is removed as duplicate, only "c3" remains)
        assertEquals(FeedSectionType.REVIEW_TODAY, sections[2].type)
        assertEquals(1, sections[2].items.size)
        assertEquals("c3", sections[2].items[0].cardId.value)
        
        // 4. Explore ("c3" is removed as duplicate, only "c4" remains)
        assertEquals(FeedSectionType.EXPLORE, sections[3].type)
        assertEquals(1, sections[3].items.size)
        assertEquals("c4", sections[3].items[0].cardId.value)
        
        // Total actionable = 4
        assertEquals(4, decision.summary.totalActionableItems)
    }

    @Test
    fun testEmptySectionHandling() {
        val engine = FeedEngine()
        
        val recommendation = createMockRecommendation(null, emptyList()) // Empty focus & explore
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList()) // Empty continue
        val reviewQueue = ReviewQueue(emptyList()) // Empty review
        
        val decisionWithHide = engine.composeFeed(
            recommendation, createMockProgress(), reviewQueue, 
            KnowledgeDecision(KnowledgeGraph(emptyMap(), emptyList()), LearningPath(emptyList()), emptyMap()),
            memory, createBasicProfile(), FeedPolicy(hideEmptySections = true)
        )
        
        // Only PROGRESS and ACHIEVEMENT should remain
        assertEquals(2, decisionWithHide.feed.sections.size)
        assertEquals(FeedSectionType.PROGRESS, decisionWithHide.feed.sections[0].type)
        assertEquals(FeedSectionType.ACHIEVEMENT, decisionWithHide.feed.sections[1].type)
        
        val decisionWithoutHide = engine.composeFeed(
            recommendation, createMockProgress(), reviewQueue, 
            KnowledgeDecision(KnowledgeGraph(emptyMap(), emptyList()), LearningPath(emptyList()), emptyMap()),
            memory, createBasicProfile(), FeedPolicy(hideEmptySections = false)
        )
        
        // All 6 sections should remain
        assertEquals(6, decisionWithoutHide.feed.sections.size)
    }

    @Test
    fun testProgressAndAchievementSection() {
        val engine = FeedEngine()
        val mockProgress = createMockProgress()
        
        val recommendation = createMockRecommendation(null, emptyList())
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())
        
        val decision = engine.composeFeed(
            recommendation, mockProgress, reviewQueue, 
            KnowledgeDecision(KnowledgeGraph(emptyMap(), emptyList()), LearningPath(emptyList()), emptyMap()),
            memory, createBasicProfile(), FeedPolicy()
        )
        
        val progressSection = decision.feed.sections.find { it.type == FeedSectionType.PROGRESS }
        val achievementSection = decision.feed.sections.find { it.type == FeedSectionType.ACHIEVEMENT }
        
        assertNotNull(progressSection)
        assertNotNull(achievementSection)
        
        assertEquals(mockProgress, progressSection?.summaryData)
        assertEquals(mockProgress, achievementSection?.summaryData)
    }
}
