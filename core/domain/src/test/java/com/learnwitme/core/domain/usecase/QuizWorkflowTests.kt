package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeQuizRepository : QuizRepository {
    var quiz: Quiz? = null
    var savedResult: QuizResult? = null
    override suspend fun getQuiz(id: QuizId) = quiz
    override suspend fun saveQuiz(quiz: Quiz) {}
    override suspend fun getQuizResult(id: QuizResultId) = savedResult
    override suspend fun saveQuizResult(result: QuizResult) { savedResult = result }
    override suspend fun getQuizResultsByUserId(userId: UserId) = if (savedResult != null) listOf(savedResult!!) else emptyList()
}

class QuizWorkflowTests {
    
    private val clock = FakeClock()
    private val quizRepo = FakeQuizRepository()
    private val roadmapRepo = FakeRoadmapRepo()
    private val memoryRepo = FakeMemoryRepo()
    private val profileRepo = FakeProfileRepo()
    private val sessionRepo = FakeSessionRepo()
    private val packageRepo = FakePackageRepo()
    
    private val memoryEngine = LearningMemoryEngine()
    private val reviewEngine = ReviewEngine()
    private val progressEngine = ProgressEngine()
    private val recommendationEngine = RecommendationEngine()
    
    private val startWorkflow = StartQuizWorkflow(quizRepo, clock)
    private val submitWorkflow = SubmitQuizAnswerWorkflow()
    private val finishWorkflow = FinishQuizWorkflow(
        quizRepo, roadmapRepo, memoryRepo, profileRepo, sessionRepo, packageRepo,
        memoryEngine, reviewEngine, progressEngine, recommendationEngine, clock
    )

    private val userId = UserId("user1")
    private val quizId = QuizId("qz1")
    private val cardId = KnowledgeCardId("c1")
    
    private val quizQuestion = QuizQuestion(
        id = "q1",
        questionText = "Question 1",
        options = listOf(QuizOption("A", "Exp A"), QuizOption("B", "Exp B")),
        correctOptionIndex = 0
    )
    
    private val quiz = Quiz(quizId, "SQL Basics", listOf(quizQuestion), DifficultyLevel.BEGINNER)

    @Test
    fun testQuizFullFlow_CorrectAnswer() = runBlocking {
        // Setup repositories
        quizRepo.quiz = quiz
        
        val unit = LearningUnit(LearningUnitId("u1"), "Unit 1", cardId, quizId, 10, "tp_sql")
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("Goal", "cp_dev", DifficultyLevel.MID), listOf(unit), RoadmapStatus.ACTIVE, emptySet())
        roadmapRepo.roadmap = roadmap
        
        memoryRepo.memory = LearningMemory(LearningMemoryId("m1"), userId, emptyList())
        profileRepo.profile = UserProfile(userId, CareerPath("cp_dev", "Dev", ""), LearningPreference(listOf("tp_sql"), 30), DifficultyLevel.BEGINNER, DifficultyLevel.MID, LearningProgress(0, 0, 0, 0))
        
        val card = KnowledgeCard(
            cardId, KnowledgePackageId("p1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best", 
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact", 
            emptyList(), emptyList(), listOf("tp_sql")
        )
        packageRepo.pkg = KnowledgePackage(
            id = KnowledgePackageId("p1"),
            title = "Pkg",
            description = "Desc",
            version = 1,
            status = PackageStatus.PUBLISHED,
            topics = listOf(Topic("tp_sql", "SQL", "SQL")),
            cards = listOf(card)
        )

        // 1. Start Quiz
        val attempt = startWorkflow.execute(userId, quizId)
        assertEquals(quizId, attempt.quiz.id)
        assertEquals(userId, attempt.userId)
        assertFalse(attempt.isCompleted)
        
        // 2. Submit Answer (Correct)
        val updatedAttempt = submitWorkflow.execute(attempt, "q1", 0)
        assertEquals(1, updatedAttempt.answers.size)
        assertEquals(0, updatedAttempt.answers[0].selectedOptionIndex)
        
        // 3. Finish Quiz
        val decision = finishWorkflow.execute(userId, updatedAttempt)
        
        // Assertions
        val result = decision.result
        assertEquals(1.0, result.score, 0.001)
        assertTrue(result.answers[0].isCorrect)
        
        // Memory Update
        val savedMemory = memoryRepo.savedMemory
        assertNotNull(savedMemory)
        val record = savedMemory!!.findRecord(cardId)
        assertNotNull(record)
        assertEquals(MemoryState.LEARNING, record!!.state)
        assertTrue(record.retention > 0.0)
        
        // Roadmap Completed unit check
        val savedRoadmap = roadmapRepo.savedRoadmap
        assertNotNull(savedRoadmap)
        assertTrue(savedRoadmap!!.completedUnitIds.contains(LearningUnitId("u1")))
        assertEquals(RoadmapStatus.COMPLETED, savedRoadmap.status)

        // Progress Update
        val savedProfile = profileRepo.savedProfile
        assertNotNull(savedProfile)
        assertEquals(1, savedProfile!!.progress.totalUnitsCompleted)
        
        // Recommendation Refresh check
        assertNotNull(decision.updatedRecommendation)
    }

    @Test
    fun testQuizFullFlow_WrongAnswer() = runBlocking {
        // Setup repositories
        quizRepo.quiz = quiz
        
        val unit = LearningUnit(LearningUnitId("u1"), "Unit 1", cardId, quizId, 10, "tp_sql")
        val roadmap = LearningRoadmap(LearningRoadmapId("r1"), LearningGoal("Goal", "cp_dev", DifficultyLevel.MID), listOf(unit), RoadmapStatus.ACTIVE, emptySet())
        roadmapRepo.roadmap = roadmap
        
        memoryRepo.memory = LearningMemory(LearningMemoryId("m1"), userId, emptyList())
        profileRepo.profile = UserProfile(userId, CareerPath("cp_dev", "Dev", ""), LearningPreference(listOf("tp_sql"), 30), DifficultyLevel.BEGINNER, DifficultyLevel.MID, LearningProgress(0, 0, 0, 0))
        
        val card = KnowledgeCard(
            cardId, KnowledgePackageId("p1"), "Title", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best", 
            emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact", 
            emptyList(), emptyList(), listOf("tp_sql")
        )
        packageRepo.pkg = KnowledgePackage(
            id = KnowledgePackageId("p1"),
            title = "Pkg",
            description = "Desc",
            version = 1,
            status = PackageStatus.PUBLISHED,
            topics = listOf(Topic("tp_sql", "SQL", "SQL")),
            cards = listOf(card)
        )

        // 1. Start Quiz
        val attempt = startWorkflow.execute(userId, quizId)
        
        // 2. Submit Answer (Wrong)
        val updatedAttempt = submitWorkflow.execute(attempt, "q1", 1)
        
        // 3. Finish Quiz
        val decision = finishWorkflow.execute(userId, updatedAttempt)
        
        // Assertions
        val result = decision.result
        assertEquals(0.0, result.score, 0.001)
        assertFalse(result.answers[0].isCorrect)
        
        // Memory Update
        val savedMemory = memoryRepo.savedMemory
        assertNotNull(savedMemory)
        val record = savedMemory!!.findRecord(cardId)
        assertNotNull(record)
        assertEquals(MemoryState.LEARNING, record!!.state) 
        assertEquals(0.1, record.retention, 0.1)
    }
}
