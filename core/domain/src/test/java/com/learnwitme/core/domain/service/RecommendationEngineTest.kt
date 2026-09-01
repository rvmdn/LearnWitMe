package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class RecommendationEngineTest {

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

    private fun createGoal(): LearningGoal {
        return LearningGoal("Become a Kotlin Dev", "cp_dev", DifficultyLevel.MID)
    }

    private fun createCard(id: String, topic: String, diff: DifficultyLevel): KnowledgeCard {
        return KnowledgeCard(
            id = KnowledgeCardId(id),
            packageId = KnowledgePackageId("p1"),
            title = "Title", summary = "Summary", whyItMatters = "Why",
            realWorldExample = "Ex", commonMistakes = "Mistakes", whenNotToUse = "Not",
            bestPractice = "Best", reference = emptyList(), officialDocumentationLink = "",
            difficulty = diff, estimatedReadingTimeSeconds = 60, careerImpact = "High",
            tags = emptyList(), prerequisiteCardIds = emptyList(), relatedTopicCodes = emptyList()
        )
    }

    @Test
    fun testReviewQueuePriorityAndMasteredFiltering() {
        val engine = RecommendationEngine()
        val card1 = createCard("c1", "tp_kotlin", DifficultyLevel.BEGINNER)
        val card2 = createCard("c2", "tp_kotlin", DifficultyLevel.BEGINNER)
        
        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 10, "tp_kotlin")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 10, "tp_kotlin")
        
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1, unit2), RoadmapStatus.ACTIVE, emptySet())
        val pkg = KnowledgePackage(id = KnowledgePackageId("p1"), title = "Pkg 1", description = "Desc", version = 1, status = PackageStatus.PUBLISHED, topics = emptyList(), cards = listOf(card1, card2))
        
        // Card 1 is Mastered but NOT in review queue. Should be filtered out.
        // Card 2 is Mastered AND in review queue (CRITICAL). Should be recommended.
        
        val memRecord1 = MemoryRecord(card1.id, MemoryState.MASTERED, 0.9, 10.0, 0.9)
        val memRecord2 = MemoryRecord(card2.id, MemoryState.MASTERED, 0.4, 1.0, 0.5)
        
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), listOf(memRecord1, memRecord2))
        
        val reviewDecision = ReviewDecision(card2.id, 1000L, ReviewPriority.CRITICAL, true, false, "Overdue")
        val reviewQueue = ReviewQueue(listOf(reviewDecision))
        
        val decision = engine.generateRecommendation(
            roadmap, memory, reviewQueue, createGoal(), createBasicProfile(), pkg, RecommendationPolicy()
        )
        
        val top = decision.topRecommendation
        assertNotNull(top)
        assertEquals(unit2.id, top!!.unit.id)
        assertTrue(top.reasons.contains("Critical review priority"))
        
        // Card 1 must be filtered out, so alternatives should be empty
        assertTrue(decision.alternatives.isEmpty())
    }

    @Test
    fun testDeterministicBehaviourAndRanking() {
        val engine = RecommendationEngine()
        val card1 = createCard("c1", "tp_kotlin", DifficultyLevel.BEGINNER)
        val card2 = createCard("c2", "tp_java", DifficultyLevel.BEGINNER)
        
        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 10, "tp_kotlin")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 10, "tp_java")
        
        // Roadmap: unit1 is pending, unit2 is pending.
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1, unit2), RoadmapStatus.ACTIVE, emptySet())
        val pkg = KnowledgePackage(id = KnowledgePackageId("p1"), title = "Pkg 1", description = "Desc", version = 1, status = PackageStatus.PUBLISHED, topics = emptyList(), cards = listOf(card1, card2))
        
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())
        
        val dec1 = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createBasicProfile(), pkg, RecommendationPolicy())
        val dec2 = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createBasicProfile(), pkg, RecommendationPolicy())
        
        assertEquals(dec1.topRecommendation?.unit?.id, dec2.topRecommendation?.unit?.id)
        assertEquals(dec1.topRecommendation?.score, dec2.topRecommendation?.score)
        
        // Unit 1 should be ranked higher because it is the next pending unit
        assertEquals(unit1.id, dec1.topRecommendation?.unit?.id)
        assertEquals(unit2.id, dec1.alternatives.first().unit.id)
    }

    @Test
    fun testWeakTopicAndDifficultyMatching() {
        val engine = RecommendationEngine()
        // c1 is BEGINNER (Matches current level) and NEW
        val card1 = createCard("c1", "tp_weak", DifficultyLevel.BEGINNER)
        // c2 is SENIOR (Does not match level) and NEW
        val card2 = createCard("c2", "tp_strong", DifficultyLevel.SENIOR)
        
        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 10, "tp_weak")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 10, "tp_strong")
        
        // We will make unit1 and unit2 the next pending units (competing) by completing the dummy
        val dummyUnit = LearningUnit(LearningUnitId("u0"), "Unit 0", KnowledgeCardId("c0"), null, 10, "tp_other")
        val dummyCard = createCard("c0", "tp_other", DifficultyLevel.BEGINNER)
        
        // Another card to trigger the Weak Topic logic for "tp_weak"
        val cardWeakHistory = createCard("c_weak", "tp_weak", DifficultyLevel.BEGINNER)
        val unitWeakHistory = LearningUnit(LearningUnitId("u_weak"), "Unit weak", cardWeakHistory.id, null, 10, "tp_weak")
        
        val roadmap = LearningRoadmap(
            LearningRoadmapId("r1"), 
            createGoal(), 
            listOf(dummyUnit, unitWeakHistory, unit1, unit2), 
            RoadmapStatus.ACTIVE, 
            setOf(dummyUnit.id, unitWeakHistory.id) // dummy and unitWeakHistory are completed, so unit1 is next pending
        )
        val pkg = KnowledgePackage(id = KnowledgePackageId("p1"), title = "Pkg 1", description = "Desc", version = 1, status = PackageStatus.PUBLISHED, topics = emptyList(), cards = listOf(dummyCard, card1, card2, cardWeakHistory))
        
        // Make tp_weak a weak topic by having a FORGOTTEN record for cardWeakHistory
        val memRecordWeakHistory = MemoryRecord(cardWeakHistory.id, MemoryState.FORGOTTEN, 0.0, 1.0, 0.0)
        
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), listOf(memRecordWeakHistory))
        val reviewQueue = ReviewQueue(emptyList()) // No review priorities to isolate scores
        
        val decision = engine.generateRecommendation(
            roadmap, memory, reviewQueue, createGoal(), createBasicProfile(), pkg, RecommendationPolicy()
        )
        
        val top = decision.topRecommendation
        assertNotNull(top)
        assertEquals(unit1.id, top!!.unit.id)
        assertTrue(top.reasons.contains("Addresses a weak topic"))
        assertTrue(top.reasons.contains("Matches your difficulty level"))
        assertTrue(top.reasons.contains("Next pending unit in your roadmap"))
    }
}
