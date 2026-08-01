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
    private val importer = mockk<PackageImporter>(relaxed = true)
    private val clock = mockk<Clock>(relaxed = true).apply {
        io.mockk.every { currentTimeMillis() } returns 1000L
    }

    private val workflow = InitializeApplicationWorkflow(
        profileRepo, roadmapRepo, memoryRepo, importer, clock
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
    fun testReturningUser_doesNotSeed() = runTest {
        val existingProfile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.BEGINNER,
            LearningProgress(0, 0, 0, 0L)
        )
        coEvery { profileRepo.getUserProfile(userId) } returns existingProfile

        workflow.execute(userId) { mockk() }

        coVerify(exactly = 0) { importer.import(any()) }
        coVerify(exactly = 0) { roadmapRepo.saveRoadmap(any(), any()) }
    }
}
