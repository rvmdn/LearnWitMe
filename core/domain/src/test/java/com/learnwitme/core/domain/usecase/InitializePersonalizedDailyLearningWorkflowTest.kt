package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference as MemoryLearningPreference
import com.learnwitme.core.domain.model.memory.personalization.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializePersonalizedDailyLearningWorkflowTest {

    private val buildContextWorkflow = mockk<BuildPersonalizedLearningContextWorkflow>()
    private val profileRepository = mockk<UserProfileRepository>()
    private val memoryRepository = mockk<LearningMemoryRepository>()
    private val roadmapRepository = mockk<RoadmapRepository>()
    private val packageRepository = mockk<KnowledgePackageRepository>()
    private val sessionRepository = mockk<LearningSessionRepository>(relaxed = true)

    private val reviewEngine = ReviewEngine()
    private val progressEngine = ProgressEngine()
    private val recommendationEngine = RecommendationEngine()
    private val knowledgeEngine = KnowledgeEngine()
    private val feedEngine = FeedEngine()

    private val clock = object : Clock {
        override fun currentTimeMillis(): Long = 1700000000000L
    }

    private val workflow = InitializePersonalizedDailyLearningWorkflow(
        buildContextWorkflow,
        profileRepository,
        memoryRepository,
        roadmapRepository,
        packageRepository,
        sessionRepository,
        reviewEngine,
        progressEngine,
        recommendationEngine,
        knowledgeEngine,
        feedEngine,
        clock
    )

    private val userId = UserId("guest_123")

    private fun createValidProfile(): UserProfile {
        return UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Build Kotlin Apps"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
    }

    private fun createValidRoadmap(): LearningRoadmap {
        return LearningRoadmap(
            id = LearningRoadmapId("rm_kotlin"),
            goal = LearningGoal("Become Kotlin Dev", "cp_kotlin_dev", DifficultyLevel.MID),
            units = listOf(
                LearningUnit(LearningUnitId("u1"), "Kotlin Basics", KnowledgeCardId("c1"), QuizId("q1"), 15, "tp_kotlin")
            ),
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = emptySet()
        )
    }

    private fun createValidPackage(): KnowledgePackage {
        val content = KnowledgeContent(
            title = "Variables & Data Types",
            summary = "Core concepts",
            whyItMatters = "Fundamental",
            realWorldExample = "val x = 1",
            commonMistakes = "none",
            whenNotToUse = "none",
            bestPractice = "use val",
            officialDocumentationLink = ""
        )
        val card = KnowledgeCard(
            id = KnowledgeCardId("c1"),
            packageId = KnowledgePackageId("pkg_kotlin"),
            content = content,
            difficulty = DifficultyLevel.BEGINNER
        )
        return KnowledgePackage(
            id = KnowledgePackageId("pkg_kotlin"),
            title = "Kotlin Package",
            description = "Desc",
            topics = listOf(Topic("tp_kotlin", "Kotlin", "Desc")),
            cards = listOf(card)
        )
    }

    @Test
    fun execute_newGuestValidPreference_returnsPersonalizedDailyFeed() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.Success)
        val success = result as DailyLearningInitializationResult.Success
        assertEquals(userId, success.userId)
        assertNotNull(success.feedDecision)
        assertEquals(DailyInitializationState.NEW_GUEST_INITIALIZED, success.initializationState)

    }

    @Test
    fun execute_existingLearnerValidPreference_returnsPersonalizedDailyFeed() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        val memory = LearningMemory(
            id = LearningMemoryId("mem_guest_123"),
            userId = userId,
            records = listOf(
                MemoryRecord(
                    cardId = KnowledgeCardId("c1"),
                    state = MemoryState.LEARNING,
                    retention = 0.8,
                    stability = 1.0,
                    confidence = 0.9
                )
            ),
            preference = MemoryLearningPreference.undetermined(1000L),
            pattern = LearningPattern.empty(1000L)
        )

        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns memory
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.Success)
        val success = result as DailyLearningInitializationResult.Success
        assertEquals(DailyInitializationState.RETURNING_USER, success.initializationState)
    }

    @Test
    fun execute_incompletePersonalization_returnsTypedIncompleteState() = runTest {
        val incompleteReason = PersonalizedContextResult.Incomplete.EmptyPreferredTopics()
        coEvery { buildContextWorkflow.execute(userId, any()) } returns incompleteReason

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.IncompletePersonalization)
        assertEquals(incompleteReason, (result as DailyLearningInitializationResult.IncompletePersonalization).reason)
    }

    @Test
    fun execute_dueReviewPrioritization_prioritizesReviewQueue() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        val review = MemoryReview(timestamp = 500L, accuracy = 0.4, hintUsed = false, confidenceRating = 0.3)
        val memory = LearningMemory(
            id = LearningMemoryId("mem_guest_123"),
            userId = userId,
            records = listOf(
                MemoryRecord(
                    cardId = KnowledgeCardId("c1"),
                    state = MemoryState.LEARNING,
                    retention = 0.2,
                    stability = 0.5,
                    confidence = 0.3,
                    reviews = listOf(review)
                )
            ),
            preference = MemoryLearningPreference.undetermined(1000L),
            pattern = LearningPattern.empty(1000L)
        )

        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns memory
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.Success)
        val success = result as DailyLearningInitializationResult.Success
        assertEquals(DailyInitializationState.REVIEW_DUE_PRIORITIZED, success.initializationState)
    }

    @Test
    fun execute_deterministicOrdering_sameInputsProduceSameFeed() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result1 = workflow.execute(userId)
        val result2 = workflow.execute(userId)

        assertTrue(result1 is DailyLearningInitializationResult.Success)
        assertTrue(result2 is DailyLearningInitializationResult.Success)
        assertEquals(
            (result1 as DailyLearningInitializationResult.Success).feedDecision,
            (result2 as DailyLearningInitializationResult.Success).feedDecision
        )
    }

    @Test
    fun execute_noMutationOfLearningMemory_memoryStateUnchanged() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        workflow.execute(userId)

        coVerify(exactly = 0) { memoryRepository.saveMemory(any()) }
        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
    }

    @Test
    fun execute_noEligibleContent_returnsContentUnavailable() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns null
        coEvery { roadmapRepository.getRoadmap(any()) } returns null

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.ContentUnavailable)
    }

    @Test
    fun execute_compatibilityWithBuildDailyFeedWorkflow() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result = workflow.execute(userId)
        assertTrue(result is DailyLearningInitializationResult.Success)
    }

    @Test
    fun execute_guestUserBehavior_worksWithoutAuthentication() = runTest {
        val guestUserId = UserId("guest_anon")
        val profile = createValidProfile().copy(id = guestUserId)
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(guestUserId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(guestUserId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(guestUserId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(guestUserId) } returns createValidRoadmap()
        coEvery { roadmapRepository.getRoadmap(any()) } returns createValidRoadmap()
        coEvery { packageRepository.getActivePackages() } returns listOf(createValidPackage())
        coEvery { packageRepository.getPackage(any()) } returns createValidPackage()

        val result = workflow.execute(guestUserId)

        assertTrue(result is DailyLearningInitializationResult.Success)
        assertEquals(guestUserId, (result as DailyLearningInitializationResult.Success).userId)
    }

    @Test
    fun execute_withDefaultIdPackage_producesActionableDailyFeedWithHeroCard() = runTest {
        val packageId = KnowledgePackageId("pkg_default_id")
        val cardId = KnowledgeCardId("c_kotlin_basics")
        val card = KnowledgeCard(
            id = cardId,
            packageId = packageId,
            content = KnowledgeContent("Sintaks Dasar", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best"),
            difficulty = DifficultyLevel.BEGINNER
        )
        val defaultPkg = KnowledgePackage(
            id = packageId,
            title = "Kurikulum Dasar Kotlin",
            description = "Dasar Kotlin",
            topics = listOf(Topic("tp_kotlin", "Kotlin", "Desc")),
            cards = listOf(card)
        )
        val roadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_default"),
            goal = LearningGoal("Goal", "cp_kotlin_dev", DifficultyLevel.MID),
            units = listOf(LearningUnit(LearningUnitId("u_kotlin_basics"), "Unit 1", cardId, null, 5, "tp_kotlin")),
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = emptySet()
        )

        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { buildContextWorkflow.execute(userId, any()) } returns PersonalizedContextResult.Success(context)
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null
        coEvery { roadmapRepository.getActiveRoadmapByUserId(userId) } returns roadmap
        coEvery { roadmapRepository.getRoadmap(any()) } returns roadmap
        coEvery { packageRepository.getActivePackages() } returns listOf(defaultPkg)
        coEvery { packageRepository.getPackage(packageId) } returns defaultPkg

        val result = workflow.execute(userId)

        assertTrue(result is DailyLearningInitializationResult.Success)
        val success = result as DailyLearningInitializationResult.Success
        val focusSection = success.feedDecision.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull("Today's Focus section must exist", focusSection)
        assertTrue("Today's Focus must contain items", focusSection!!.items.isNotEmpty())
        assertEquals("c_kotlin_basics", focusSection.items.first().cardId.value)
    }
}

