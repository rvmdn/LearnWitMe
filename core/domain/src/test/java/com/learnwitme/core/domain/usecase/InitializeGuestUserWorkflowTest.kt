package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPreference
import com.learnwitme.core.domain.repository.UserProfileRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeGuestUserWorkflowTest {

    private val profileRepository = mockk<UserProfileRepository>()
    private val workflow = InitializeGuestUserWorkflow(profileRepository)

    @Test
    fun execute_noExistingProfile_initializesGuestIdentityAndPersists() = runTest {
        val guestIdentity = UserIdentity.Guest()
        coEvery { profileRepository.getUserProfile(guestIdentity.id) } returns null
        coEvery { profileRepository.saveUserProfile(any()) } just Runs

        val profile = workflow.execute(guestIdentity)

        assertNotNull(profile)
        assertEquals(guestIdentity.id, profile.id)
        assertEquals("cp_kotlin_dev", profile.careerPath.code)
        assertEquals(DifficultyLevel.BEGINNER, profile.currentLevel)
        assertEquals(DifficultyLevel.MID, profile.targetLevel)
        coVerify(exactly = 1) { profileRepository.saveUserProfile(profile) }
    }

    @Test
    fun execute_existingProfile_returnsExistingProfileWithoutReinitialization() = runTest {
        val guestIdentity = UserIdentity.Guest()
        val existingProfile = UserProfile(
            id = guestIdentity.id,
            careerPath = CareerPath("cp_existing", "Existing Dev", "Description"),
            preference = LearningPreference(listOf("tp_existing"), 45),
            currentLevel = DifficultyLevel.MID,
            targetLevel = DifficultyLevel.SENIOR,
            progress = LearningProgress(5, 10, 3, 1000L)
        )
        coEvery { profileRepository.getUserProfile(guestIdentity.id) } returns existingProfile

        val result = workflow.execute(guestIdentity)

        assertEquals(existingProfile, result)
        assertEquals("cp_existing", result.careerPath.code)
        assertEquals(DifficultyLevel.MID, result.currentLevel)
        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
    }

    @Test
    fun execute_identityPersistence_profileSavedInRepository() = runTest {
        val fakeRepo = object : UserProfileRepository {
            private val map = mutableMapOf<UserId, UserProfile>()
            override suspend fun getUserProfile(userId: UserId): UserProfile? = map[userId]
            override suspend fun saveUserProfile(profile: UserProfile) {
                map[profile.id] = profile
            }
        }
        val workflowWithFake = InitializeGuestUserWorkflow(fakeRepo)
        val guestIdentity = UserIdentity.Guest()

        assertNull(fakeRepo.getUserProfile(guestIdentity.id))
        val createdProfile = workflowWithFake.execute(guestIdentity)
        
        val retrievedProfile = fakeRepo.getUserProfile(guestIdentity.id)
        assertNotNull(retrievedProfile)
        assertEquals(createdProfile, retrievedProfile)
    }

    @Test
    fun execute_repeatedInitialization_doesNotCreateConflictingIdentities() = runTest {
        val fakeRepo = object : UserProfileRepository {
            private val map = mutableMapOf<UserId, UserProfile>()
            override suspend fun getUserProfile(userId: UserId): UserProfile? = map[userId]
            override suspend fun saveUserProfile(profile: UserProfile) {
                map[profile.id] = profile
            }
        }
        val workflowWithFake = InitializeGuestUserWorkflow(fakeRepo)
        val guestIdentity = UserIdentity.Guest()

        val first = workflowWithFake.execute(guestIdentity)
        val second = workflowWithFake.execute(guestIdentity)
        val third = workflowWithFake.execute(guestIdentity)

        assertEquals(first, second)
        assertEquals(second, third)
        assertEquals(0, fakeRepo.getUserProfile(guestIdentity.id)?.progress?.totalUnitsCompleted ?: -1)
    }

    @Test
    fun execute_deterministicStartupState_returnsConsistentIdentity() = runTest {
        val guestIdentity = UserIdentity.Guest()
        coEvery { profileRepository.getUserProfile(guestIdentity.id) } returns null
        coEvery { profileRepository.saveUserProfile(any()) } just Runs

        val firstRun = workflow.execute(guestIdentity)
        val secondRun = workflow.execute(guestIdentity)

        assertEquals(firstRun.id, secondRun.id)
        assertEquals(firstRun.careerPath, secondRun.careerPath)
        assertEquals(firstRun.preference, secondRun.preference)
    }

    @Test
    fun execute_repositoryFailure_handlesOrPropagatesError() = runTest {
        val guestIdentity = UserIdentity.Guest()
        coEvery { profileRepository.getUserProfile(guestIdentity.id) } throws RuntimeException("Database error")

        try {
            workflow.execute(guestIdentity)
            fail("Expected RuntimeException was not thrown")
        } catch (e: Exception) {
            assertTrue(e is RuntimeException)
            assertEquals("Database error", e.message)
        }
    }
}
