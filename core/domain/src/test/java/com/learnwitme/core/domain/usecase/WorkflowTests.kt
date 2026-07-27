package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeMemoryRepo : LearningMemoryRepository {
    var memory: LearningMemory? = null
    var savedMemory: LearningMemory? = null
    val snapshots = mutableListOf<com.learnwitme.core.domain.model.memory.LearningMemorySnapshot>()

    override suspend fun getMemory(id: LearningMemoryId) = memory
    override suspend fun getMemoryByUserId(userId: UserId) = memory
    override suspend fun saveMemory(memory: LearningMemory) { savedMemory = memory }
    override suspend fun deleteMemoryByUserId(userId: UserId) { memory = null; savedMemory = null }
    override suspend fun saveSnapshot(snapshot: com.learnwitme.core.domain.model.memory.LearningMemorySnapshot) { snapshots.add(snapshot) }
    override suspend fun getSnapshot(snapshotId: String) = snapshots.find { it.snapshotId == snapshotId }
    override suspend fun getSnapshotsForMemory(memoryId: LearningMemoryId) = snapshots.filter { it.memoryId == memoryId }
    override suspend fun getLatestSnapshot(memoryId: LearningMemoryId) = snapshots.filter { it.memoryId == memoryId }.maxByOrNull { it.capturedEpochMs }
    override suspend fun getSnapshotsInRange(memoryId: LearningMemoryId, fromEpochMs: Long, toEpochMs: Long) =
        snapshots.filter { it.memoryId == memoryId && it.capturedEpochMs in fromEpochMs..toEpochMs }
    override suspend fun pruneSnapshotsBefore(memoryId: LearningMemoryId, beforeEpochMs: Long) {
        snapshots.removeAll { it.memoryId == memoryId && it.capturedEpochMs < beforeEpochMs }
    }
}

class FakeRoadmapRepo : RoadmapRepository {
    var roadmap: LearningRoadmap? = null
    var savedRoadmap: LearningRoadmap? = null
    override suspend fun getRoadmap(id: LearningRoadmapId) = roadmap
    override suspend fun getActiveRoadmapByUserId(userId: UserId) = roadmap
    override suspend fun saveRoadmap(userId: UserId, roadmap: LearningRoadmap) { savedRoadmap = roadmap }
}

class FakeProfileRepo : UserProfileRepository {
    var profile: UserProfile? = null
    var savedProfile: UserProfile? = null
    override suspend fun getUserProfile(userId: UserId) = profile
    override suspend fun saveUserProfile(profile: UserProfile) { savedProfile = profile }
}

class FakePackageRepo : KnowledgePackageRepository {
    var pkg: KnowledgePackage? = null
    override suspend fun getPackage(id: KnowledgePackageId) = pkg
    override suspend fun savePackage(pkg: KnowledgePackage) { this.pkg = pkg }
    override suspend fun getActivePackages() = if (pkg != null) listOf(pkg!!) else emptyList()
}

class FakeSessionRepo : LearningSessionRepository {
    var sessions = mutableListOf<LearningSession>()
    var savedSession: LearningSession? = null
    override suspend fun getSessions(userId: UserId) = sessions
    override suspend fun saveSession(userId: UserId, session: LearningSession) {
        savedSession = session
        sessions.add(session)
    }
}

class FakeClock : Clock {
    override fun currentTimeMillis() = 1000L
}

class WorkflowTests {

    @Test
    fun testCompleteLearningSessionWorkflow() = runBlocking {
        val memoryRepo = FakeMemoryRepo()
        val roadmapRepo = FakeRoadmapRepo()
        val profileRepo = FakeProfileRepo()
        val sessionRepo = FakeSessionRepo()
        val clock = FakeClock()
        val progressEngine = ProgressEngine()
        
        val userId = UserId("u1")
        val roadmapId = LearningRoadmapId("r1")
        
        val unit = LearningUnit(LearningUnitId("u1"), "U1", KnowledgeCardId("c1"), null, 10, "t1")
        val roadmap = LearningRoadmap(roadmapId, LearningGoal("G", "cp_dev", DifficultyLevel.MID), listOf(unit), RoadmapStatus.ACTIVE, emptySet())
        roadmapRepo.roadmap = roadmap
        
        memoryRepo.memory = LearningMemory(LearningMemoryId("m1"), userId, emptyList())
        profileRepo.profile = UserProfile(userId, CareerPath("cp_dev", "Developer", ""), LearningPreference(listOf("t"), 30), DifficultyLevel.BEGINNER, DifficultyLevel.MID, LearningProgress(0, 0, 0, 0))
        
        val workflow = CompleteLearningSessionWorkflow(
            memoryRepo, roadmapRepo, profileRepo, sessionRepo, progressEngine, clock
        )
        
        val session = LearningSession(LearningSessionId("s1"), 100L, 200L, listOf(LearningUnitId("u1")))
        workflow.execute(userId, roadmapId, session)
        
        // Assertions
        assertNotNull(roadmapRepo.savedRoadmap)
        assertTrue(roadmapRepo.savedRoadmap!!.completedUnitIds.contains(LearningUnitId("u1")))
        assertEquals(RoadmapStatus.COMPLETED, roadmapRepo.savedRoadmap!!.status)
        
        assertNotNull(sessionRepo.savedSession)
        assertEquals(session.id, sessionRepo.savedSession!!.id)
        
        assertNotNull(profileRepo.savedProfile)
        assertEquals(1, profileRepo.savedProfile!!.progress.totalUnitsCompleted) // Calculated by progress engine
    }

    @Test(expected = IllegalStateException::class)
    fun testErrorPropagationIfMemoryMissing() = runBlocking {
        val memoryRepo = FakeMemoryRepo()
        val engine = LearningMemoryEngine()
        val clock = FakeClock()
        
        val workflow = FinishReviewWorkflow(memoryRepo, engine, clock)
        
        // memory is null in fake repo
        workflow.execute(UserId("u1"), KnowledgeCardId("c1"), ReviewQuality(1.0, false, 10, 1.0)) { "t" }
    }
}
