package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class DailyFactSelectionTest {

    private val engine = RecommendationEngine()
    private val feedEngine = FeedEngine()

    private fun createProfile(
        level: DifficultyLevel = DifficultyLevel.BEGINNER,
        preferredTopics: List<String> = listOf("tp_kotlin")
    ): UserProfile {
        return UserProfile(
            id = UserId("u1"),
            careerPath = CareerPath("cp_android", "Android Engineer", ""),
            preference = LearningPreference(preferredTopics, 30),
            currentLevel = level,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0)
        )
    }

    private fun createGoal(): LearningGoal = LearningGoal("Master Android", "cp_android", DifficultyLevel.MID)

    private fun createCard(
        id: String,
        topic: String,
        diff: DifficultyLevel = DifficultyLevel.BEGINNER,
        prereqs: List<String> = emptyList()
    ): KnowledgeCard {
        return KnowledgeCard(
            id = KnowledgeCardId(id),
            packageId = KnowledgePackageId("pkg_default"),
            title = "Title $id",
            summary = "Summary $id",
            whyItMatters = "Why $id",
            realWorldExample = "Ex $id",
            commonMistakes = "Mistake $id",
            whenNotToUse = "WhenNot $id",
            bestPractice = "Best $id",
            reference = emptyList(),
            officialDocumentationLink = "",
            difficulty = diff,
            estimatedReadingTimeSeconds = 120,
            careerImpact = "High",
            tags = emptyList(),
            prerequisiteCardIds = prereqs.map { KnowledgeCardId(it) },
            relatedTopicCodes = listOf(topic)
        )
    }

    private fun createPackage(id: String, cards: List<KnowledgeCard>): KnowledgePackage {
        return KnowledgePackage(
            id = KnowledgePackageId(id),
            title = "Pkg $id",
            description = "Desc $id",
            version = 1,
            checksum = "valid_checksum",
            isCore = false,
            status = PackageStatus.PUBLISHED,
            topics = emptyList(),
            cards = cards
        )
    }

    @Test
    fun testNewUser_selectsValidFirstRoadmapCard() {
        val card1 = createCard("c_kotlin_basics", "tp_kotlin", DifficultyLevel.BEGINNER)
        val card2 = createCard("c_kotlin_val_var", "tp_kotlin", DifficultyLevel.BEGINNER)

        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 5, "tp_kotlin")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 5, "tp_kotlin")

        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1, unit2), RoadmapStatus.ACTIVE, emptySet())
        val pkg = createPackage("pkg_default", listOf(card1, card2))
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())

        val decision = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createProfile(), pkg, RecommendationPolicy())

        assertNotNull(decision.topRecommendation)
        assertEquals(unit1.id, decision.topRecommendation!!.unit.id)
        assertEquals(card1.id, decision.topRecommendation!!.unit.cardId)
    }

    @Test
    fun testUserWithDueReview_reviewReceivesPriorityOverNewCard() {
        val card1 = createCard("c_kotlin_basics", "tp_kotlin", DifficultyLevel.BEGINNER)
        val card2 = createCard("c_kotlin_val_var", "tp_kotlin", DifficultyLevel.BEGINNER)

        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 5, "tp_kotlin")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 5, "tp_kotlin")

        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1, unit2), RoadmapStatus.ACTIVE, emptySet())
        val pkg = createPackage("pkg_default", listOf(card1, card2))

        // Card 1 was learned previously and is now due for review
        val record1 = MemoryRecord(card1.id, MemoryState.LEARNING, 0.3, 1.0, 0.4)
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), listOf(record1))

        val reviewDecision = ReviewDecision(card1.id, 1000L, ReviewPriority.CRITICAL, true, false, "Due for review")
        val reviewQueue = ReviewQueue(listOf(reviewDecision))

        val decision = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createProfile(), pkg, RecommendationPolicy())

        assertNotNull(decision.topRecommendation)
        // Review of card1 must beat new material card2
        assertEquals(unit1.id, decision.topRecommendation!!.unit.id)
        assertTrue(decision.topRecommendation!!.reasons.any { it.contains("review priority") })
    }

    @Test
    fun testCompletedNonDueCard_isExcludedFromNewSelection() {
        val card1 = createCard("c_kotlin_basics", "tp_kotlin", DifficultyLevel.BEGINNER)
        val card2 = createCard("c_kotlin_val_var", "tp_kotlin", DifficultyLevel.BEGINNER)

        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 5, "tp_kotlin")
        val unit2 = LearningUnit(LearningUnitId("u2"), "Unit 2", card2.id, null, 5, "tp_kotlin")

        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1, unit2), RoadmapStatus.ACTIVE, setOf(unit1.id))
        val pkg = createPackage("pkg_default", listOf(card1, card2))

        // Card 1 is Mastered and NOT due for review
        val record1 = MemoryRecord(card1.id, MemoryState.MASTERED, 0.95, 10.0, 0.9)
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), listOf(record1))
        val reviewQueue = ReviewQueue(emptyList()) // No due reviews

        val decision = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createProfile(), pkg, RecommendationPolicy())

        assertNotNull(decision.topRecommendation)
        // Unit 2 must be recommended, Unit 1 must be excluded
        assertEquals(unit2.id, decision.topRecommendation!!.unit.id)
        assertTrue("Alternatives must not contain mastered non-due card", decision.alternatives.none { it.unit.id == unit1.id })
    }

    @Test
    fun testFeedEngine_producesActionableHeroCardInDailyFeed() {
        val card1 = createCard("c_kotlin_basics", "tp_kotlin", DifficultyLevel.BEGINNER)
        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", card1.id, null, 5, "tp_kotlin")
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1), RoadmapStatus.ACTIVE, emptySet())
        val pkg = createPackage("pkg_default", listOf(card1))
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())

        val recDecision = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createProfile(), pkg, RecommendationPolicy())
        val feedDecision = feedEngine.composeFeed(
            recommendation = recDecision,
            progress = ProgressSummary(0.0, 0.0, 0, 1, 0, 0.0),
            reviewQueue = reviewQueue,
            knowledge = KnowledgeDecision(
                graph = KnowledgeGraph(emptyMap(), emptyList()),
                learningPath = LearningPath(emptyList()),
                availabilityMap = emptyMap()
            ),
            memory = memory,
            profile = createProfile(),
            policy = FeedPolicy()
        )

        val todaysFocus = feedDecision.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull(todaysFocus)
        assertTrue("Today's focus must have actionable items", todaysFocus!!.items.isNotEmpty())
        assertEquals(card1.id, todaysFocus.items.first().cardId)
        assertTrue(feedDecision.summary.totalActionableItems >= 1)
    }

    @Test
    fun testEmptyPackage_returnsNullRecommendation() {
        val unit1 = LearningUnit(LearningUnitId("u1"), "Unit 1", KnowledgeCardId("c_missing"), null, 5, "tp_kotlin")
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(unit1), RoadmapStatus.ACTIVE, emptySet())
        val pkg = createPackage("pkg_default", emptyList())
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())

        val decision = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), createProfile(), pkg, RecommendationPolicy())
        assertNull(decision.topRecommendation)
        assertTrue(decision.alternatives.isEmpty())
    }

    @Test
    fun testMultiTopicMultiDifficulty_deterministicRanking() {
        val cardKotlin = createCard("c_k1", "tp_kotlin", DifficultyLevel.BEGINNER)
        val cardAndroid = createCard("c_a1", "tp_android", DifficultyLevel.MID)
        val cardArch = createCard("c_ar1", "tp_architecture", DifficultyLevel.SENIOR)

        val u1 = LearningUnit(LearningUnitId("u1"), "U1", cardKotlin.id, null, 5, "tp_kotlin")
        val u2 = LearningUnit(LearningUnitId("u2"), "U2", cardAndroid.id, null, 5, "tp_android")
        val u3 = LearningUnit(LearningUnitId("u3"), "U3", cardArch.id, null, 5, "tp_architecture")

        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(u1, u2, u3), RoadmapStatus.ACTIVE, emptySet())
        val pkg = createPackage("pkg_default", listOf(cardKotlin, cardAndroid, cardArch))
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), emptyList())
        val reviewQueue = ReviewQueue(emptyList())

        // User profile is BEGINNER with Kotlin interest
        val profile = createProfile(DifficultyLevel.BEGINNER, listOf("tp_kotlin"))

        val dec1 = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), profile, pkg, RecommendationPolicy())
        val dec2 = engine.generateRecommendation(roadmap, memory, reviewQueue, createGoal(), profile, pkg, RecommendationPolicy())

        // Must be completely deterministic
        assertEquals(dec1.topRecommendation?.unit?.id, dec2.topRecommendation?.unit?.id)
        assertEquals(u1.id, dec1.topRecommendation?.unit?.id)
    }

    @Test
    fun testUserPersonalization_differentUsersProduceDifferentRankings() {
        val cardAndroid = createCard("c_a1", "tp_android", DifficultyLevel.MID)
        val cardArch = createCard("c_ar1", "tp_architecture", DifficultyLevel.MID)

        val u1 = LearningUnit(LearningUnitId("u1"), "Android Unit", cardAndroid.id, null, 5, "tp_android")
        val u2 = LearningUnit(LearningUnitId("u2"), "Architecture Unit", cardArch.id, null, 5, "tp_architecture")

        // No pending unit priority by passing null/empty roadmap units or completed first unit
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), createGoal(), listOf(u1, u2), RoadmapStatus.ACTIVE, setOf(u1.id, u2.id))
        val pkg = createPackage("pkg_default", listOf(cardAndroid, cardArch))

        // User A prefers Android, User B prefers Architecture
        val profileA = createProfile(DifficultyLevel.MID, listOf("tp_android"))
        val profileB = createProfile(DifficultyLevel.MID, listOf("tp_architecture"))

        // Both cards have review scheduled
        val revA = ReviewDecision(cardAndroid.id, 1000L, ReviewPriority.HIGH, true, false, "Overdue")
        val revB = ReviewDecision(cardArch.id, 1000L, ReviewPriority.HIGH, true, false, "Overdue")
        val queue = ReviewQueue(listOf(revA, revB))
        val memory = LearningMemory(LearningMemoryId("m1"), UserId("u1"), listOf(
            MemoryRecord(cardAndroid.id, MemoryState.LEARNING, 0.5, 1.0, 0.5),
            MemoryRecord(cardArch.id, MemoryState.LEARNING, 0.5, 1.0, 0.5)
        ))

        val decisionA = engine.generateRecommendation(roadmap, memory, queue, createGoal(), profileA, pkg, RecommendationPolicy())
        val decisionB = engine.generateRecommendation(roadmap, memory, queue, createGoal(), profileB, pkg, RecommendationPolicy())

        assertEquals("User A should be recommended Android card", u1.id, decisionA.topRecommendation?.unit?.id)
        assertEquals("User B should be recommended Architecture card", u2.id, decisionB.topRecommendation?.unit?.id)
    }
}
