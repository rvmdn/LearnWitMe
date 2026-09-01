package com.learnwitme.app.runtime

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.app.seeder.DefaultPackageParser
import com.learnwitme.app.ui.startup.StartupState
import com.learnwitme.app.ui.startup.StartupViewModel
import com.learnwitme.core.config.LocalizationManager
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import com.learnwitme.core.domain.usecase.*
import com.learnwitme.feature.dailyfeed.DailyFeedState
import com.learnwitme.feature.dailyfeed.DailyFeedViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class RuntimeStartupToDailyFeedIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    // Context & Assets
    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()

    // In-memory repositories
    private var userProfile: UserProfile? = null
    private var learningMemory: LearningMemory? = null
    private var learningRoadmap: LearningRoadmap? = null
    private val sessions = mutableListOf<LearningSession>()
    private val packages = mutableMapOf<String, KnowledgePackage>()
    private val cards = mutableMapOf<String, KnowledgeCard>()
    private val quizzes = mutableMapOf<String, Quiz>()
    private val quizResults = mutableListOf<QuizResult>()

    private val profileRepo = object : UserProfileRepository {
        override suspend fun getUserProfile(userId: UserId): UserProfile? = userProfile
        override suspend fun saveUserProfile(profile: UserProfile) { userProfile = profile }
    }

    private val memoryRepo = object : LearningMemoryRepository {
        override suspend fun getMemory(id: LearningMemoryId): LearningMemory? = learningMemory
        override suspend fun getMemoryByUserId(userId: UserId): LearningMemory? = learningMemory
        override suspend fun saveMemory(memory: LearningMemory) { learningMemory = memory }
        override suspend fun deleteMemoryByUserId(userId: UserId) { learningMemory = null }
        override suspend fun saveSnapshot(snapshot: com.learnwitme.core.domain.model.memory.LearningMemorySnapshot) {}
        override suspend fun getSnapshot(snapshotId: String): com.learnwitme.core.domain.model.memory.LearningMemorySnapshot? = null
        override suspend fun getSnapshotsForMemory(memoryId: LearningMemoryId): List<com.learnwitme.core.domain.model.memory.LearningMemorySnapshot> = emptyList()
        override suspend fun getLatestSnapshot(memoryId: LearningMemoryId): com.learnwitme.core.domain.model.memory.LearningMemorySnapshot? = null
        override suspend fun getSnapshotsInRange(memoryId: LearningMemoryId, fromEpochMs: Long, toEpochMs: Long): List<com.learnwitme.core.domain.model.memory.LearningMemorySnapshot> = emptyList()
        override suspend fun pruneSnapshotsBefore(memoryId: LearningMemoryId, beforeEpochMs: Long) {}
    }

    private val roadmapRepo = object : RoadmapRepository {
        override suspend fun getRoadmap(id: LearningRoadmapId): LearningRoadmap? = learningRoadmap
        override suspend fun getActiveRoadmapByUserId(userId: UserId): LearningRoadmap? = learningRoadmap
        override suspend fun saveRoadmap(userId: UserId, roadmap: LearningRoadmap) { learningRoadmap = roadmap }
    }

    private val sessionRepo = object : LearningSessionRepository {
        override suspend fun getSessions(userId: UserId): List<LearningSession> = sessions
        override suspend fun saveSession(userId: UserId, session: LearningSession) { sessions.add(session) }
    }

    private val packageRepo = object : KnowledgePackageRepository {
        override suspend fun getPackage(id: KnowledgePackageId): KnowledgePackage? = packages[id.value]
        override suspend fun savePackage(pkg: KnowledgePackage) { packages[pkg.id.value] = pkg }
        override suspend fun getActivePackages(): List<KnowledgePackage> = packages.values.toList()
    }

    private val knowledgeRepo = object : KnowledgeRepository {
        override suspend fun getCard(id: KnowledgeCardId): KnowledgeCard? = cards[id.value]
        override suspend fun findCard(id: KnowledgeCardId): KnowledgeCard? = cards[id.value]
        override suspend fun saveCard(card: KnowledgeCard) { cards[card.id.value] = card }
        override suspend fun deleteCard(id: KnowledgeCardId) { cards.remove(id.value) }
        override suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> = cards.values.filter { it.relatedTopicCodes.contains(topicCode) }
        override suspend fun searchBySkills(skills: List<com.learnwitme.core.domain.model.Skill>): List<KnowledgeCard> = emptyList()
        override suspend fun findPackage(packageId: KnowledgePackageId): KnowledgePackage? = null
        override suspend fun importPackage(pkg: KnowledgePackage) {}
        override suspend fun publishPackage(packageId: KnowledgePackageId) {}
    }

    private val quizRepo = object : QuizRepository {
        override suspend fun getQuiz(id: QuizId): Quiz? = quizzes[id.value]
        override suspend fun saveQuiz(quiz: Quiz) { quizzes[quiz.id.value] = quiz }
        override suspend fun getQuizResult(id: QuizResultId): QuizResult? = quizResults.find { it.id == id }
        override suspend fun saveQuizResult(result: QuizResult) { quizResults.add(result) }
        override suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult> = quizResults
    }

    // Domain Engines & Clock
    private val clock = object : Clock {
        override fun currentTimeMillis(): Long = 1000L
    }
    private val reviewEngine = ReviewEngine()
    private val progressEngine = ProgressEngine()
    private val recommendationEngine = RecommendationEngine()
    private val knowledgeEngine = KnowledgeEngine()
    private val feedEngine = FeedEngine()

    private val localizationManager = mockk<LocalizationManager>()
    private val userId = UserIdentity.Guest.DEFAULT_GUEST_ID

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { localizationManager.currentLanguage } returns MutableStateFlow("id")

        // Asset setup
        every { context.assets } returns assetManager
        val assetsDir = listOf(
            File("src/main/assets"),
            File("app/src/main/assets"),
            File("../app/src/main/assets")
        ).find { it.exists() && it.isDirectory } ?: File("src/main/assets")

        every { assetManager.open("default-package.json") } answers {
            FileInputStream(File(assetsDir, "default-package.json"))
        }
        every { assetManager.open("default-package-en.json") } throws Exception("Not found")
        every { assetManager.open("default-package-id.json") } answers {
            FileInputStream(File(assetsDir, "default-package-id.json"))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testEndToEndRuntimeFlow_startupToDailyFeedActionableContent() = runTest(testDispatcher) {
        val packageValidator = PackageValidator(packageRepo, knowledgeRepo)
        val packageImporter = PackageImporter(packageRepo, knowledgeRepo, quizRepo, packageValidator)

        // 1. Application Startup Initialization Workflow
        val initWorkflow = InitializeApplicationWorkflow(
            profileRepo, roadmapRepo, memoryRepo, packageRepo, packageImporter, clock
        )
        val buildContextWorkflow = BuildPersonalizedLearningContextWorkflow(
            profileRepo, memoryRepo, clock
        )
        val determineStartupWorkflow = DetermineUserStartupStateWorkflow(
            profileRepo, buildContextWorkflow
        )

        // 2. StartupViewModel execution
        val startupViewModel = StartupViewModel(
            initializeApplicationWorkflow = initWorkflow,
            determineUserStartupStateWorkflow = determineStartupWorkflow,
            localizationManager = localizationManager,
            context = context
        )
        advanceUntilIdle()

        // P38.3 Verification 1: Startup transitions to NewGuestOnboarding without exception
        assertTrue(
            "Startup state should be NewGuestOnboarding for first launch",
            startupViewModel.state.value is StartupState.NewGuestOnboarding
        )

        // P38.3 Verification 2: Package exists in Repository with cards
        val activePackages = packageRepo.getActivePackages()
        assertEquals(1, activePackages.size)
        val savedPkg = activePackages.first()
        assertEquals(KnowledgePackageId("pkg_default_id"), savedPkg.id)
        assertTrue("Saved package must contain cards", savedPkg.cards.isNotEmpty())
        for (c in savedPkg.cards) {
            assertEquals("Card ${c.id.value} must match package ID", savedPkg.id, c.packageId)
        }

        // P38.3 Verification 3: Roadmap references an existing card in package
        val activeRoadmap = roadmapRepo.getActiveRoadmapByUserId(userId)
        assertNotNull("Active roadmap must exist", activeRoadmap)
        assertTrue("Roadmap units must not be empty", activeRoadmap!!.units.isNotEmpty())
        val firstUnit = activeRoadmap.units.first()
        val referencedCard = savedPkg.cards.find { it.id == firstUnit.cardId }
        assertNotNull("Roadmap unit cardId must exist in the saved package", referencedCard)

        // 3. User Completes Profile / Preferences (Onboarding completion)
        val profile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Android Kotlin Developer", "Build modern Android apps"),
            preference = LearningPreference(preferredTopics = listOf("tp_kotlin"), dailyLimitMinutes = 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(totalUnitsCompleted = 0, totalCardsMastered = 0, activeStreakDays = 0, lastActiveDate = 1000L)
        )
        profileRepo.saveUserProfile(profile)

        // 4. Daily Feed Workflow Execution
        val dailyWorkflow = InitializePersonalizedDailyLearningWorkflow(
            buildPersonalizedLearningContextWorkflow = buildContextWorkflow,
            profileRepository = profileRepo,
            memoryRepository = memoryRepo,
            roadmapRepository = roadmapRepo,
            packageRepository = packageRepo,
            sessionRepository = sessionRepo,
            reviewEngine = reviewEngine,
            progressEngine = progressEngine,
            recommendationEngine = recommendationEngine,
            knowledgeEngine = knowledgeEngine,
            feedEngine = feedEngine,
            clock = clock
        )

        val dailyResult = dailyWorkflow.execute(userId)
        assertTrue("DailyLearningInitializationResult must be Success", dailyResult is DailyLearningInitializationResult.Success)
        val success = dailyResult as DailyLearningInitializationResult.Success

        // P38.3 Verification 4 & 5: FeedEngine produces actionable TODAYS_FOCUS item
        val todaysFocus = success.feedDecision.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull("Today's Focus section must exist in feed", todaysFocus)
        assertTrue("Today's Focus section must contain items", todaysFocus!!.items.isNotEmpty())
        assertEquals(firstUnit.cardId, todaysFocus.items.first().cardId)

        // 5. DailyFeedViewModel State Verification
        val dailyViewModel = DailyFeedViewModel(dailyWorkflow)
        advanceUntilIdle()

        assertTrue("DailyFeedViewModel state must be Success", dailyViewModel.uiState.value is DailyFeedState.Success)
        val feedState = dailyViewModel.uiState.value as DailyFeedState.Success
        val viewModelHeroSection = feedState.feed.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull(viewModelHeroSection)
        assertEquals(firstUnit.cardId, viewModelHeroSection!!.items.first().cardId)
    }
}
