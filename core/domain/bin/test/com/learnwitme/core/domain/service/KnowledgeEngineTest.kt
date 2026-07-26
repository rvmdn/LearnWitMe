package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class KnowledgeEngineTest {

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

    private fun createCard(id: String, prereqs: List<String>): KnowledgeCard {
        return KnowledgeCard(
            id = KnowledgeCardId(id),
            title = "Title \$id", summary = "Summary", whyItMatters = "Why",
            realWorldExample = "Ex", commonMistakes = "Mistakes", whenNotToUse = "Not",
            bestPractice = "Best", reference = emptyList(), officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER, estimatedReadingTimeSeconds = 60, careerImpact = "High",
            tags = emptyList(), prerequisiteCardIds = prereqs.map { KnowledgeCardId(it) }, relatedTopicCodes = emptyList()
        )
    }

    @Test
    fun testTopologicalSortForLearningPath() {
        val engine = KnowledgeEngine()
        // C depends on B, B depends on A
        val cardA = createCard("A", emptyList())
        val cardB = createCard("B", listOf("A"))
        val cardC = createCard("C", listOf("B"))
        
        val pkg = KnowledgePackage(KnowledgePackageId("p1"), "Pkg", "Desc", 1, PackageStatus.PUBLISHED, emptyList(), listOf(cardC, cardB, cardA))
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("G", "cp", DifficultyLevel.MID), emptyList(), RoadmapStatus.ACTIVE, emptySet())
        
        val decision = engine.processKnowledge(pkg, roadmap, createBasicProfile(), KnowledgePolicy())
        
        // Path should be A -> B -> C
        assertEquals(3, decision.learningPath.orderedCardIds.size)
        assertEquals("A", decision.learningPath.orderedCardIds[0].value)
        assertEquals("B", decision.learningPath.orderedCardIds[1].value)
        assertEquals("C", decision.learningPath.orderedCardIds[2].value)
    }

    @Test(expected = IllegalStateException::class)
    fun testCircularDependencyDetection() {
        val engine = KnowledgeEngine()
        // A depends on B, B depends on C, C depends on A
        val cardA = createCard("A", listOf("B"))
        val cardB = createCard("B", listOf("C"))
        val cardC = createCard("C", listOf("A"))
        
        val pkg = KnowledgePackage(KnowledgePackageId("p1"), "Pkg", "Desc", 1, PackageStatus.PUBLISHED, emptyList(), listOf(cardC, cardB, cardA))
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("G", "cp", DifficultyLevel.MID), emptyList(), RoadmapStatus.ACTIVE, emptySet())
        
        // Should throw IllegalStateException
        engine.processKnowledge(pkg, roadmap, createBasicProfile(), KnowledgePolicy())
    }

    @Test
    fun testPrerequisiteAvailabilityCheck() {
        val engine = KnowledgeEngine()
        val cardA = createCard("A", emptyList())
        val cardB = createCard("B", listOf("A"))
        
        val pkg = KnowledgePackage(KnowledgePackageId("p1"), "Pkg", "Desc", 1, PackageStatus.PUBLISHED, emptyList(), listOf(cardA, cardB))
        
        // Roadmap where A is completed
        val unitA = LearningUnit(LearningUnitId("uA"), "Unit A", cardA.id, null, 10, "topic")
        val unitB = LearningUnit(LearningUnitId("uB"), "Unit B", cardB.id, null, 10, "topic")
        
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("G", "cp", DifficultyLevel.MID), listOf(unitA, unitB), RoadmapStatus.ACTIVE, setOf(unitA.id))
        
        val decision = engine.processKnowledge(pkg, roadmap, createBasicProfile(), KnowledgePolicy())
        
        // Card A should be available (no prereqs)
        assertTrue(decision.availabilityMap[cardA.id]!!.isAvailable)
        
        // Card B should be available because A is completed in roadmap
        assertTrue(decision.availabilityMap[cardB.id]!!.isAvailable)
        assertTrue(decision.availabilityMap[cardB.id]!!.unmetPrerequisites.isEmpty())
        
        // If we use a roadmap where A is NOT completed
        val emptyRoadmap = LearningRoadmap(LearningRoadmapId("r2"), LearningGoal("G", "cp", DifficultyLevel.MID), listOf(unitA, unitB), RoadmapStatus.ACTIVE, emptySet())
        val decision2 = engine.processKnowledge(pkg, emptyRoadmap, createBasicProfile(), KnowledgePolicy())
        
        assertFalse(decision2.availabilityMap[cardB.id]!!.isAvailable)
        assertTrue(decision2.availabilityMap[cardB.id]!!.unmetPrerequisites.contains(cardA.id))
    }

    @Test
    fun testDeterministicBehaviour() {
        val engine = KnowledgeEngine()
        // D and C both have no prerequisites. B depends on C and D. A depends on B.
        // Even if we shuffle the package, D and C should always sort lexicographically C then D.
        val cardA = createCard("A", listOf("B"))
        val cardB = createCard("B", listOf("C", "D"))
        val cardC = createCard("C", emptyList())
        val cardD = createCard("D", emptyList())
        
        val pkg1 = KnowledgePackage(KnowledgePackageId("p1"), "Pkg", "Desc", 1, PackageStatus.PUBLISHED, emptyList(), listOf(cardA, cardB, cardC, cardD))
        val pkg2 = KnowledgePackage(KnowledgePackageId("p1"), "Pkg", "Desc", 1, PackageStatus.PUBLISHED, emptyList(), listOf(cardD, cardC, cardB, cardA))
        
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("G", "cp", DifficultyLevel.MID), emptyList(), RoadmapStatus.ACTIVE, emptySet())
        
        val decision1 = engine.processKnowledge(pkg1, roadmap, createBasicProfile(), KnowledgePolicy())
        val decision2 = engine.processKnowledge(pkg2, roadmap, createBasicProfile(), KnowledgePolicy())
        
        assertEquals(decision1.learningPath.orderedCardIds, decision2.learningPath.orderedCardIds)
        // Order should be C, D, B, A
        assertEquals("C", decision1.learningPath.orderedCardIds[0].value)
        assertEquals("D", decision1.learningPath.orderedCardIds[1].value)
        assertEquals("B", decision1.learningPath.orderedCardIds[2].value)
        assertEquals("A", decision1.learningPath.orderedCardIds[3].value)
    }
}
