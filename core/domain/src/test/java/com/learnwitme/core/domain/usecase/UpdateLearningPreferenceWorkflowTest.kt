package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.UserProfileRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateLearningPreferenceWorkflowTest {

    private val profileRepository = mockk<UserProfileRepository>()
    private val workflow = UpdateLearningPreferenceWorkflow(profileRepository)
    private val userId = UserId("current_user")

    private fun createInitialProfile(): UserProfile {
        return UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Description"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(2, 5, 1, 500L)
        )
    }

    @Test
    fun execute_validPreference_updatesAndPersistsProfile() = runTest {
        val initialProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns initialProfile
        coEvery { profileRepository.saveUserProfile(any()) } just Runs

        val newCareer = CareerPath("cp_android_dev", "Android Developer", "Build Apps")
        val updated = workflow.execute(
            userId = userId,
            preferredTopics = listOf("tp_kotlin", "tp_android"),
            dailyLimitMinutes = 45,
            careerPath = newCareer,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.SENIOR
        )

        assertNotNull(updated)
        assertEquals(listOf("tp_kotlin", "tp_android"), updated.preference.preferredTopics)
        assertEquals(45, updated.preference.dailyLimitMinutes)
        assertEquals(newCareer, updated.careerPath)
        assertEquals(DifficultyLevel.SENIOR, updated.targetLevel)
        coVerify(exactly = 1) { profileRepository.saveUserProfile(updated) }
    }

    @Test
    fun execute_invalidPreference_emptyTopics_throwsIllegalArgumentException() = runTest {
        val initialProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns initialProfile

        try {
            workflow.execute(
                userId = userId,
                preferredTopics = emptyList(),
                dailyLimitMinutes = 30,
                careerPath = initialProfile.careerPath,
                currentLevel = DifficultyLevel.BEGINNER,
                targetLevel = DifficultyLevel.MID
            )
            fail("Expected IllegalArgumentException for empty topics")
        } catch (e: IllegalArgumentException) {
            assertEquals("Preferred topics cannot be empty", e.message)
        }
        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
    }

    @Test
    fun execute_invalidPreference_nonPositiveDailyLimit_throwsIllegalArgumentException() = runTest {
        val initialProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns initialProfile

        try {
            workflow.execute(
                userId = userId,
                preferredTopics = listOf("tp_kotlin"),
                dailyLimitMinutes = 0,
                careerPath = initialProfile.careerPath,
                currentLevel = DifficultyLevel.BEGINNER,
                targetLevel = DifficultyLevel.MID
            )
            fail("Expected IllegalArgumentException for zero daily limit")
        } catch (e: IllegalArgumentException) {
            assertEquals("Daily learning limit must be positive", e.message)
        }
        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
    }

    @Test
    fun execute_invalidPreference_invalidLevelCombination_throwsIllegalArgumentException() = runTest {
        val initialProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns initialProfile

        try {
            workflow.execute(
                userId = userId,
                preferredTopics = listOf("tp_kotlin"),
                dailyLimitMinutes = 30,
                careerPath = initialProfile.careerPath,
                currentLevel = DifficultyLevel.SENIOR,
                targetLevel = DifficultyLevel.BEGINNER
            )
            fail("Expected IllegalArgumentException for invalid level ordinal")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("cannot be higher than target level") == true)
        }
        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
    }

    @Test
    fun execute_persistenceThroughRepositoryAbstraction_profileSavedAndRetrievable() = runTest {
        val fakeRepo = object : UserProfileRepository {
            private var profile: UserProfile? = createInitialProfile()
            override suspend fun getUserProfile(userId: UserId): UserProfile? = profile
            override suspend fun saveUserProfile(profile: UserProfile) {
                this.profile = profile
            }
        }
        val workflowWithFake = UpdateLearningPreferenceWorkflow(fakeRepo)

        val updated = workflowWithFake.execute(
            userId = userId,
            preferredTopics = listOf("tp_kotlin", "tp_compose"),
            dailyLimitMinutes = 60,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Description"),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID
        )

        val retrieved = fakeRepo.getUserProfile(userId)
        assertNotNull(retrieved)
        assertEquals(updated, retrieved)
        assertEquals(listOf("tp_kotlin", "tp_compose"), retrieved?.preference?.preferredTopics)
    }

    @Test
    fun execute_updatingExistingGuestProfile_preservesUserIdAndProgress() = runTest {
        val initialProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns initialProfile
        coEvery { profileRepository.saveUserProfile(any()) } just Runs

        val updated = workflow.execute(
            userId = userId,
            preferredTopics = listOf("tp_kotlin"),
            dailyLimitMinutes = 20,
            careerPath = initialProfile.careerPath,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID
        )

        assertEquals(userId, updated.id)
        assertEquals(initialProfile.progress, updated.progress)
    }

    @Test
    fun execute_preferenceSurvivesReload_reloadingFromRepositoryReturnsUpdatedPreference() = runTest {
        val fakeRepo = object : UserProfileRepository {
            private var storedProfile: UserProfile? = createInitialProfile()
            override suspend fun getUserProfile(userId: UserId): UserProfile? = storedProfile
            override suspend fun saveUserProfile(profile: UserProfile) {
                storedProfile = profile
            }
        }
        val workflowWithFake = UpdateLearningPreferenceWorkflow(fakeRepo)

        workflowWithFake.execute(
            userId = userId,
            preferredTopics = listOf("tp_kotlin", "tp_coroutine"),
            dailyLimitMinutes = 40,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Description"),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID
        )

        val reloaded = fakeRepo.getUserProfile(userId)
        assertEquals(listOf("tp_kotlin", "tp_coroutine"), reloaded?.preference?.preferredTopics)
        assertEquals(40, reloaded?.preference?.dailyLimitMinutes)
    }

    @Test
    fun execute_noMutationOfOriginalImmutableDomainObject() = runTest {
        val originalProfile = createInitialProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns originalProfile
        coEvery { profileRepository.saveUserProfile(any()) } just Runs

        val updated = workflow.execute(
            userId = userId,
            preferredTopics = listOf("tp_kotlin", "tp_new"),
            dailyLimitMinutes = 90,
            careerPath = originalProfile.careerPath,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID
        )

        assertNotEquals(originalProfile, updated)
        assertEquals(listOf("tp_kotlin"), originalProfile.preference.preferredTopics)
        assertEquals(30, originalProfile.preference.dailyLimitMinutes)
        assertEquals(listOf("tp_kotlin", "tp_new"), updated.preference.preferredTopics)
    }

    @Test
    fun execute_missingUserProfile_throwsIllegalStateException() = runTest {
        coEvery { profileRepository.getUserProfile(userId) } returns null

        try {
            workflow.execute(
                userId = userId,
                preferredTopics = listOf("tp_kotlin"),
                dailyLimitMinutes = 30,
                careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Description"),
                currentLevel = DifficultyLevel.BEGINNER,
                targetLevel = DifficultyLevel.MID
            )
            fail("Expected IllegalStateException for missing profile")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("UserProfile not found") == true)
        }
    }
}
