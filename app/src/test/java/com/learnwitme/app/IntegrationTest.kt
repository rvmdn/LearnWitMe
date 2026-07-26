package com.learnwitme.app

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import com.learnwitme.core.domain.usecase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntegrationTest {

    private val memoryEngine = LearningMemoryEngine()
    private val reviewEngine = ReviewEngine()
    private val progressEngine = ProgressEngine()
    private val recommendationEngine = RecommendationEngine()
    private val knowledgeEngine = KnowledgeEngine()
    private val feedEngine = FeedEngine()

    private val clock = object : Clock {
        override fun currentTimeMillis(): Long = 1000L
    }

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

    private val progressRepo = object : ProgressRepository {
        override suspend fun getProgress(userId: UserId): LearningProgress? = userProfile?.progress
        override suspend fun saveProgress(userId: UserId, progress: LearningProgress) {
            userProfile = userProfile?.updateProgress(progress)
        }
    }

    private val packageRepo = object : KnowledgePackageRepository {
        override suspend fun getPackage(id: KnowledgePackageId): KnowledgePackage? = packages[id.value]
        override suspend fun savePackage(pkg: KnowledgePackage) { packages[pkg.id.value] = pkg }
        override suspend fun getActivePackages(): List<KnowledgePackage> = packages.values.toList()
    }

    private val knowledgeRepo = object : KnowledgeRepository {
        override suspend fun getCard(id: KnowledgeCardId): KnowledgeCard? = cards[id.value]
        override suspend fun saveCard(card: KnowledgeCard) { cards[card.id.value] = card }
        override suspend fun deleteCard(id: KnowledgeCardId) { cards.remove(id.value) }
        override suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> = cards.values.filter { it.relatedTopicCodes.contains(topicCode) }
    }

    private val quizRepo = object : QuizRepository {
        override suspend fun getQuiz(id: QuizId): Quiz? = quizzes[id.value]
        override suspend fun saveQuiz(quiz: Quiz) { quizzes[quiz.id.value] = quiz }
        override suspend fun getQuizResult(id: QuizResultId): QuizResult? = quizResults.find { it.id == id }
        override suspend fun saveQuizResult(result: QuizResult) { quizResults.add(result) }
        override suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult> = quizResults
    }

    private val validator = PackageValidator(packageRepo, knowledgeRepo)
    private val importer = PackageImporter(packageRepo, knowledgeRepo, quizRepo, validator)

    private val initializeWorkflow = InitializeApplicationWorkflow(profileRepo, roadmapRepo, memoryRepo, importer, clock)
    private val buildDailyFeedWorkflow = BuildDailyFeedWorkflow(
        memoryRepo, roadmapRepo, profileRepo, packageRepo, sessionRepo,
        reviewEngine, progressEngine, recommendationEngine, knowledgeEngine, feedEngine, clock
    )
    private val finishReviewWorkflow = FinishReviewWorkflow(memoryRepo, memoryEngine, clock)
    private val startQuizWorkflow = StartQuizWorkflow(quizRepo, clock)
    private val submitQuizAnswerWorkflow = SubmitQuizAnswerWorkflow()
    private val finishQuizWorkflow = FinishQuizWorkflow(
        quizRepo, roadmapRepo, memoryRepo, profileRepo, sessionRepo, packageRepo,
        memoryEngine, reviewEngine, progressEngine, recommendationEngine, clock
    )

    private val userId = UserId("test_user")

    @Test
    fun testIntegrationFlow_endToEnd() = runTest {
        assertNull(profileRepo.getUserProfile(userId))

        val topic = Topic("tp_kotlin", "Kotlin", "Basics")
        val card = KnowledgeCard(
            id = KnowledgeCardId("c_kotlin_basics"),
            title = "Syntax",
            summary = "Val and Var",
            whyItMatters = "W",
            realWorldExample = "E",
            commonMistakes = "M",
            whenNotToUse = "N",
            bestPractice = "B",
            reference = emptyList(),
            officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER,
            estimatedReadingTimeSeconds = 120,
            careerImpact = "H",
            tags = emptyList(),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = listOf("tp_kotlin")
        )
        val unit = LearningUnit(
            id = LearningUnitId("u_kotlin_basics"),
            title = "Unit 1",
            cardId = card.id,
            quizId = QuizId("qz_kotlin_basics"),
            estimatedMinutes = 5,
            topicCode = "tp_kotlin"
        )
        val quiz = Quiz(
            id = QuizId("qz_kotlin_basics"),
            title = "Kotlin Quiz",
            questions = listOf(
                QuizQuestion("q1", "Val read-only?", listOf(QuizOption("val", "ok"), QuizOption("var", "no")), 0)
            ),
            targetDifficulty = DifficultyLevel.BEGINNER
        )

        val dummyMetadata = PackageMetadata(
            id = KnowledgePackageId("pkg_default"),
            title = "Default Kotlin",
            description = "Kotlin Basics",
            version = PackageVersion(1, 0, 0),
            checksum = PackageChecksum("temp")
        )
        val dummyManifest = PackageManifest(dummyMetadata, listOf(topic), listOf(card), listOf(unit), listOf(quiz), emptyList())
        val correctChecksum = calculateChecksum(dummyManifest)
        val metadata = dummyMetadata.copy(checksum = correctChecksum)
        val manifest = dummyManifest.copy(metadata = metadata)

        initializeWorkflow.execute(userId) { manifest }

        assertNotNull(packageRepo.getPackage(metadata.id))
        assertNotNull(roadmapRepo.getActiveRoadmapByUserId(userId))
        assertNotNull(memoryRepo.getMemoryByUserId(userId))

        val profile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.MID,
            LearningProgress(0, 0, 0, 0L)
        )
        profileRepo.saveUserProfile(profile)
        assertNotNull(profileRepo.getUserProfile(userId))

        val feed = buildDailyFeedWorkflow.execute(userId)
        assertNotNull(feed)
        val focusSection = feed.feed.sections.find { it.type == FeedSectionType.TODAYS_FOCUS }
        assertNotNull(focusSection)
        assertEquals("c_kotlin_basics", focusSection!!.items.first().cardId.value)

        val quality = ReviewQuality(1.0, false, 1000L, 1.0)
        finishReviewWorkflow.execute(userId, card.id, quality) { "tp_kotlin" }

        val updatedMemory = memoryRepo.getMemoryByUserId(userId)!!
        assertEquals(1, updatedMemory.records.size)
        assertEquals(MemoryState.LEARNING, updatedMemory.records[0].state)

        val attempt = startQuizWorkflow.execute(userId, quiz.id)
        assertEquals(quiz.id, attempt.quiz.id)

        val updatedAttempt = submitQuizAnswerWorkflow.execute(attempt, "q1", 0)
        val decision = finishQuizWorkflow.execute(userId, updatedAttempt)

        val finalProfile = profileRepo.getUserProfile(userId)!!
        assertTrue(finalProfile.progress.totalUnitsCompleted > 0)

        val appRestartProfile = profileRepo.getUserProfile(userId)!!
        assertEquals(finalProfile.progress.totalUnitsCompleted, appRestartProfile.progress.totalUnitsCompleted)
    }
}
