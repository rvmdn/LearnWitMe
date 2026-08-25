package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference as MemoryLearningPreference
import com.learnwitme.core.domain.model.memory.personalization.*
import com.learnwitme.core.domain.repository.LearningMemoryRepository
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.service.Clock
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BuildPersonalizedLearningContextWorkflowTest {

    private val profileRepository = mockk<UserProfileRepository>()
    private val memoryRepository = mockk<LearningMemoryRepository>()
    private val clock = object : Clock {
        override fun currentTimeMillis(): Long = 1700000000000L
    }
    private val workflow = BuildPersonalizedLearningContextWorkflow(profileRepository, memoryRepository, clock)
    private val userId = UserId("guest_user_123")

    private fun createValidProfile(): UserProfile {
        return UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Build Android & Backend apps"),
            preference = LearningPreference(listOf("tp_kotlin", "tp_coroutine"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
    }

    @Test
    fun execute_guestProfileWithValidPreferences_returnsSuccessContext() = runTest {
        val guestProfile = createValidProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns guestProfile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Success)
        val success = result as PersonalizedContextResult.Success
        assertEquals(userId, success.context.userId)
        assertTrue(success.context.activeGoals.contains("Kotlin Developer"))
        assertTrue(success.context.activeGoals.contains("tp_kotlin"))
        assertTrue(success.context.activeGoals.contains("tp_coroutine"))
    }

    @Test
    fun execute_existingUserProfileWithValidPreferences_returnsSuccessContext() = runTest {
        val existingProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_android_dev", "Android Engineer", "Native Android"),
            preference = LearningPreference(listOf("tp_android", "tp_compose"), 60),
            currentLevel = DifficultyLevel.MID,
            targetLevel = DifficultyLevel.SENIOR,
            progress = LearningProgress(10, 25, 7, 1000L)
        )
        coEvery { profileRepository.getUserProfile(userId) } returns existingProfile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Success)
        val success = result as PersonalizedContextResult.Success
        assertEquals(userId, success.context.userId)
        assertTrue(success.context.activeGoals.contains("Android Engineer"))
        assertTrue(success.context.activeGoals.contains("tp_android"))
    }

    @Test
    fun execute_incompletePreferences_emptyTopics_returnsIncompleteState() = runTest {
        val invalidProfile = mockk<UserProfile>()
        every { invalidProfile.preference.preferredTopics } returns emptyList()
        coEvery { profileRepository.getUserProfile(userId) } returns invalidProfile

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Incomplete.EmptyPreferredTopics)
    }

    @Test
    fun execute_invalidPreferences_zeroDailyLimit_returnsIncompleteState() = runTest {
        val invalidProfile = mockk<UserProfile>()
        every { invalidProfile.preference.preferredTopics } returns listOf("tp_kotlin")
        every { invalidProfile.preference.dailyLimitMinutes } returns 0
        coEvery { profileRepository.getUserProfile(userId) } returns invalidProfile

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Incomplete.InvalidDailyLimit)
    }

    @Test
    fun execute_invalidLevelCombination_returnsIncompleteState() = runTest {
        val invalidProfile = mockk<UserProfile>()
        every { invalidProfile.preference.preferredTopics } returns listOf("tp_kotlin")
        every { invalidProfile.preference.dailyLimitMinutes } returns 30
        every { invalidProfile.currentLevel } returns DifficultyLevel.SENIOR
        every { invalidProfile.targetLevel } returns DifficultyLevel.BEGINNER
        coEvery { profileRepository.getUserProfile(userId) } returns invalidProfile

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Incomplete.InvalidLevelCombination)
    }

    @Test
    fun execute_deterministicOutputForIdenticalInput() = runTest {
        val validProfile = createValidProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns validProfile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        val run1 = workflow.execute(userId)
        val run2 = workflow.execute(userId)

        assertTrue(run1 is PersonalizedContextResult.Success)
        assertTrue(run2 is PersonalizedContextResult.Success)
        assertEquals(
            (run1 as PersonalizedContextResult.Success).context,
            (run2 as PersonalizedContextResult.Success).context
        )
    }

    @Test
    fun execute_correctMappingOfPreferenceToPersonalizationContext() = runTest {
        val profile = createValidProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        val result = workflow.execute(userId) as PersonalizedContextResult.Success
        assertEquals(listOf("Kotlin Developer", "tp_kotlin", "tp_coroutine"), result.context.activeGoals)
        assertEquals(1700000000000L, result.context.generatedEpochMs)
    }

    @Test
    fun execute_policyCompatibility_strictPrivacy() = runTest {
        val profile = createValidProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        val result = workflow.execute(userId, policy = LearningPersonalizationPolicy.STRICT_PRIVACY)

        assertTrue(result is PersonalizedContextResult.Success)
        val context = (result as PersonalizedContextResult.Success).context
        assertTrue(context.strengths.isEmpty())
        assertTrue(context.weaknesses.isEmpty())
        assertTrue(context.behaviors.isEmpty())
        assertEquals(0.0, context.overallRetentionAverage, 0.001)
        assertEquals(0, context.totalCardsTracked)
    }

    @Test
    fun execute_noRepositoryMutationWhenBuildingContext() = runTest {
        val profile = createValidProfile()
        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { memoryRepository.getMemory(any()) } returns null
        coEvery { memoryRepository.getMemoryByUserId(userId) } returns null

        workflow.execute(userId)

        coVerify(exactly = 0) { profileRepository.saveUserProfile(any()) }
        coVerify(exactly = 0) { memoryRepository.saveMemory(any()) }
    }

    @Test
    fun execute_missingProfile_returnsIncompleteMissingProfileState() = runTest {
        coEvery { profileRepository.getUserProfile(userId) } returns null

        val result = workflow.execute(userId)

        assertTrue(result is PersonalizedContextResult.Incomplete.MissingProfile)
    }
}
