package com.learnwitme.app.runtime

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.app.seeder.DefaultPackageParser
import com.learnwitme.app.ui.startup.StartupState
import com.learnwitme.app.ui.startup.StartupViewModel
import com.learnwitme.core.application.ai.feedback.AILearningInteractionService
import com.learnwitme.core.config.LocalizationManager
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import com.learnwitme.core.domain.usecase.*
import com.learnwitme.feature.dailyfeed.DailyFeedEffect
import com.learnwitme.feature.dailyfeed.DailyFeedEvent
import com.learnwitme.feature.dailyfeed.DailyFeedState
import com.learnwitme.feature.dailyfeed.DailyFeedViewModel
import com.learnwitme.feature.knowledgecard.KnowledgeCardEffect
import com.learnwitme.feature.knowledgecard.KnowledgeCardEvent
import com.learnwitme.feature.knowledgecard.KnowledgeCardState
import com.learnwitme.feature.knowledgecard.KnowledgeCardViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalCoroutinesApi::class)
class FirstLearningLoopIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    private val context = mockk<Context>()
    private val assetManager = mockk<AssetManager>()

    // Persistent storage simulation across the session
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

    private class TestClock(var timeMs: Long = 1000L) : Clock {
        override fun currentTimeMillis(): Long = timeMs
        fun advanceBy(millis: Long) {
            timeMs += millis
        }
    }

    private val clock = TestClock(1000L)

    private val memoryEngine = LearningMemoryEngine()
    private val reviewEngine = ReviewEngine()
    private val progressEngine = ProgressEngine()
    private val recommendationEngine = RecommendationEngine()
    private val knowledgeEngine = KnowledgeEngine()
    private val feedEngine = FeedEngine()

    private val localizationManager = mockk<LocalizationManager>()
    private val aiService = mockk<AILearningInteractionService>(relaxed = true)

    private val userId = UserIdentity.Guest.DEFAULT_GUEST_ID

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { localizationManager.currentLanguage } returns MutableStateFlow("id")

        val assetsDir = listOf(
            File("src/main/assets"),
            File("app/src/main/assets"),
            File("../app/src/main/assets")
        ).find { it.exists() && it.isDirectory } ?: File("src/main/assets")

        every { context.assets } returns assetManager
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
    fun testFirstLearningLoop_endToEndWithPersistenceAndAppRestart() = runTest(testDispatcher) {
        val packageValidator = PackageValidator(packageRepo, knowledgeRepo)
        val packageImporter = PackageImporter(packageRepo, knowledgeRepo, quizRepo, packageValidator)

        // -------------------------------------------------------------
        // STEP 1: Cold Start Application Initialization
        // -------------------------------------------------------------
        val initWorkflow = InitializeApplicationWorkflow(
            profileRepo, roadmapRepo, memoryRepo, packageRepo, packageImporter, clock
        )
        val buildContextWorkflow = BuildPersonalizedLearningContextWorkflow(
            profileRepo, memoryRepo, clock
        )
        val determineStartupWorkflow = DetermineUserStartupStateWorkflow(
            profileRepo, buildContextWorkflow
        )

        val startupViewModel = StartupViewModel(
            initializeApplicationWorkflow = initWorkflow,
            determineUserStartupStateWorkflow = determineStartupWorkflow,
            localizationManager = localizationManager,
            context = context
        )
        advanceUntilIdle()

        assertTrue(startupViewModel.state.value is StartupState.NewGuestOnboarding)

        // -------------------------------------------------------------
        // STEP 2: User completes onboarding / profile setup
        // -------------------------------------------------------------
        val initialProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Android Kotlin Developer", "Learn Kotlin"),
            preference = LearningPreference(preferredTopics = listOf("tp_kotlin"), dailyLimitMinutes = 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(totalUnitsCompleted = 0, totalCardsMastered = 0, activeStreakDays = 0, lastActiveDate = clock.currentTimeMillis())
        )
        profileRepo.saveUserProfile(initialProfile)

        // -------------------------------------------------------------
        // STEP 3: User opens Daily Feed and receives real KnowledgeCard
        // -------------------------------------------------------------
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

        val dailyFeedViewModel = DailyFeedViewModel(dailyWorkflow)
        advanceUntilIdle()

        assertTrue("DailyFeedViewModel must be Success", dailyFeedViewModel.uiState.value is DailyFeedState.Success)
        val feedState = dailyFeedViewModel.uiState.value as DailyFeedState.Success
        val focusSection = feedState.feed.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull("Today's Focus section must exist", focusSection)
        assertTrue("Today's Focus items must not be empty", focusSection!!.items.isNotEmpty())

        val targetCardId = focusSection.items.first().cardId
        assertEquals("c_kotlin_basics", targetCardId.value)

        // -------------------------------------------------------------
        // STEP 4: User opens and views the KnowledgeCard
        // -------------------------------------------------------------
        val loadCardWorkflow = LoadKnowledgeCardWorkflow(knowledgeRepo)
        val finishReviewWorkflow = FinishReviewWorkflow(memoryRepo, memoryEngine, clock)
        val completeLearningSessionWorkflow = CompleteLearningSessionWorkflow(
            memoryRepository = memoryRepo,
            roadmapRepository = roadmapRepo,
            profileRepository = profileRepo,
            sessionRepository = sessionRepo,
            progressEngine = progressEngine,
            clock = clock
        )

        val knowledgeCardViewModel = KnowledgeCardViewModel(
            loadCardWorkflow = loadCardWorkflow,
            finishReviewWorkflow = finishReviewWorkflow,
            completeLearningSessionWorkflow = completeLearningSessionWorkflow,
            roadmapRepository = roadmapRepo,
            clock = clock,
            aiLearningInteractionService = aiService
        )

        knowledgeCardViewModel.onEvent(KnowledgeCardEvent.OnLoadCard(targetCardId.value))
        advanceUntilIdle()

        assertTrue("KnowledgeCardState must be Success", knowledgeCardViewModel.uiState.value is KnowledgeCardState.Success)
        val cardState = knowledgeCardViewModel.uiState.value as KnowledgeCardState.Success
        assertEquals(targetCardId, cardState.card.id)
        assertEquals(KnowledgePackageId("pkg_default_id"), cardState.card.packageId)
        assertTrue("Card title should not be blank", cardState.card.title.isNotBlank())

        // -------------------------------------------------------------
        // STEP 5: User completes learning & submits review rating
        // -------------------------------------------------------------
        clock.advanceBy(60000L) // 1 minute spent reading
        knowledgeCardViewModel.onEvent(KnowledgeCardEvent.OnSubmitReview(targetCardId.value, 0.9))
        advanceUntilIdle()

        // Verify learning state is persisted to repository
        val memoryAfterReview = memoryRepo.getMemoryByUserId(userId)
        assertNotNull("Learning memory must exist after review", memoryAfterReview)
        val record = memoryAfterReview!!.findRecord(targetCardId)
        assertNotNull("Card must have an active memory record", record)
        assertTrue("Memory retention must be updated (> 0)", record!!.retention > 0.0)
        assertTrue("Review history must not be empty", record.reviews.isNotEmpty())

        // Verify session is persisted
        val recordedSessions = sessionRepo.getSessions(userId)
        assertTrue("Learning session must be recorded", recordedSessions.isNotEmpty())

        // -------------------------------------------------------------
        // STEP 6: Simulated App Restart (State Persistence & Spaced Repetition)
        // -------------------------------------------------------------
        clock.advanceBy(86400000L) // Next day (24 hours later)

        val restartedBuildContextWorkflow = BuildPersonalizedLearningContextWorkflow(
            profileRepo, memoryRepo, clock
        )
        val restartedDailyWorkflow = InitializePersonalizedDailyLearningWorkflow(
            buildPersonalizedLearningContextWorkflow = restartedBuildContextWorkflow,
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

        val restartedDailyResult = restartedDailyWorkflow.execute(userId)
        assertTrue("Daily feed after restart must be Success", restartedDailyResult is DailyLearningInitializationResult.Success)
        val restartedSuccess = restartedDailyResult as DailyLearningInitializationResult.Success

        // Verify that memory after restart preserves the learned record
        val preservedMemory = memoryRepo.getMemoryByUserId(userId)
        assertNotNull("Memory record must be preserved across app restarts", preservedMemory)
        val preservedRecord = preservedMemory!!.findRecord(targetCardId)
        assertNotNull("Record must still exist", preservedRecord)
        assertEquals(targetCardId, preservedRecord!!.cardId)
        assertEquals(MemoryState.LEARNING, preservedRecord.state)
        assertTrue("Total cards tracked must be at least 1", restartedSuccess.context.totalCardsTracked >= 1)
    }
}
