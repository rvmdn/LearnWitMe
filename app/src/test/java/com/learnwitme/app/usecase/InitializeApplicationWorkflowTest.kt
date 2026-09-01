package com.learnwitme.app.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.Clock
import com.learnwitme.core.domain.service.PackageImporter
import com.learnwitme.core.domain.usecase.InitializeApplicationWorkflow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeApplicationWorkflowTest {

    private val profileRepo = mockk<UserProfileRepository>(relaxed = true)
    private val roadmapRepo = mockk<RoadmapRepository>(relaxed = true)
    private val memoryRepo = mockk<LearningMemoryRepository>(relaxed = true)
    private val packageRepo = mockk<KnowledgePackageRepository>(relaxed = true)
    private val importer = mockk<PackageImporter>(relaxed = true)
    private val clock = mockk<Clock>(relaxed = true).apply {
        io.mockk.every { currentTimeMillis() } returns 1000L
    }

    private val workflow = InitializeApplicationWorkflow(
        profileRepo, roadmapRepo, memoryRepo, packageRepo, importer, clock
    )

    private val userId = UserId("test_user")

    @Test
    fun testFirstLaunch_seedsDefaultData() = runTest {
        coEvery { profileRepo.getUserProfile(userId) } returns null
        
        val version = PackageVersion(1, 0, 0)
        val metadata = PackageMetadata(KnowledgePackageId("p1"), "Title", "Desc", version, PackageChecksum("checksum"))
        val manifest = PackageManifest(metadata, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        
        coEvery { importer.import(manifest) } returns ImportSummary(metadata.id, version, 0, 0, 0, 0, "SUCCESS")

        workflow.execute(userId) { manifest }

        coVerify { importer.import(manifest) }
        coVerify { roadmapRepo.saveRoadmap(any(), any()) }
        coVerify { memoryRepo.saveMemory(any()) }
    }

    @Test
    fun testReturningUser_withValidData_doesNotSeed() = runTest {
        val existingProfile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.BEGINNER,
            LearningProgress(0, 0, 0, 0L)
        )
        coEvery { profileRepo.getUserProfile(userId) } returns existingProfile
        
        val validCard = KnowledgeCard(
            id = KnowledgeCardId("c1"),
            packageId = KnowledgePackageId("pkg_1"),
            content = KnowledgeContent("T", "S", "W", "E", "M", "N", "B")
        )
        val validPkg = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "T",
            description = "D",
            version = 1,
            cards = listOf(validCard)
        )
        coEvery { packageRepo.getActivePackages() } returns listOf(validPkg)
        
        val validRoadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_default"),
            goal = LearningGoal("Goal", "cp_kotlin_dev", DifficultyLevel.MID),
            units = listOf(LearningUnit(LearningUnitId("u1"), "U1", KnowledgeCardId("c1"), null, 5, "tp_kotlin")),
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = emptySet()
        )
        coEvery { roadmapRepo.getActiveRoadmapByUserId(userId) } returns validRoadmap

        workflow.execute(userId) { mockk() }

        coVerify(exactly = 0) { importer.import(any()) }
        coVerify(exactly = 0) { roadmapRepo.saveRoadmap(any(), any()) }
    }

    @Test
    fun testReturningUser_withCorruptedOrMissingPackage_selfHealsWithoutLosingProfile() = runTest {
        val existingProfile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.MID,
            LearningProgress(5, 3, 2, 1000L)
        )
        coEvery { profileRepo.getUserProfile(userId) } returns existingProfile
        
        // Simulating corrupted package: activePackages is empty or has 0 cards
        coEvery { packageRepo.getActivePackages() } returns emptyList()
        
        val existingRoadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_default"),
            goal = LearningGoal("Goal", "cp_kotlin_dev", DifficultyLevel.MID),
            units = listOf(LearningUnit(LearningUnitId("u1"), "U1", KnowledgeCardId("c1"), null, 5, "tp_kotlin")),
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = setOf(LearningUnitId("u1"))
        )
        coEvery { roadmapRepo.getActiveRoadmapByUserId(userId) } returns existingRoadmap
        coEvery { memoryRepo.getMemoryByUserId(userId) } returns mockk(relaxed = true)

        val version = PackageVersion(1, 0, 0)
        val metadata = PackageMetadata(KnowledgePackageId("pkg_default_id"), "Title", "Desc", version, PackageChecksum("checksum"))
        val card = KnowledgeCard(
            id = KnowledgeCardId("c1"),
            packageId = KnowledgePackageId("pkg_default_id"),
            content = KnowledgeContent("T", "S", "W", "E", "M", "N", "B")
        )
        val unit = LearningUnit(LearningUnitId("u1"), "U1", KnowledgeCardId("c1"), null, 5, "tp_kotlin")
        val manifest = PackageManifest(metadata, emptyList(), listOf(card), listOf(unit), emptyList(), emptyList())

        coEvery { importer.import(manifest) } returns ImportSummary(metadata.id, version, 0, 1, 1, 0, "SUCCESS")

        workflow.execute(userId) { manifest }

        // Assert package was self-healed
        coVerify(exactly = 1) { importer.import(manifest) }
        // Assert user profile was NOT touched or deleted
        coVerify(exactly = 0) { profileRepo.saveUserProfile(any()) }
    }
}
