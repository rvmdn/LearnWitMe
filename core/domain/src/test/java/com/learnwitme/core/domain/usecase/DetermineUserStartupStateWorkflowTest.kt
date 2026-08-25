package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.model.memory.personalization.PersonalizedContextResult
import com.learnwitme.core.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetermineUserStartupStateWorkflowTest {

    private val profileRepository = mockk<UserProfileRepository>()
    private val buildPersonalizedLearningContextWorkflow = mockk<BuildPersonalizedLearningContextWorkflow>()
    private val workflow = DetermineUserStartupStateWorkflow(profileRepository, buildPersonalizedLearningContextWorkflow)

    private val userId = UserIdentity.Guest.DEFAULT_GUEST_ID

    private fun createValidProfile(): UserProfile {
        return UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Build Kotlin Apps"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
    }

    @Test
    fun execute_noProfileFound_returnsNewGuest() = runTest {
        coEvery { profileRepository.getUserProfile(userId) } returns null

        val result = workflow.execute(userId)

        assertTrue(result is UserStartupState.NewGuest)
        assertEquals(userId, (result as UserStartupState.NewGuest).userId)
    }

    @Test
    fun execute_validProfileAndContext_returnsReady() = runTest {
        val profile = createValidProfile()
        val context = mockk<LearningPersonalizationContext>(relaxed = true)

        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { buildPersonalizedLearningContextWorkflow.execute(userId) } returns PersonalizedContextResult.Success(context)

        val result = workflow.execute(userId)

        assertTrue(result is UserStartupState.Ready)
        val ready = result as UserStartupState.Ready
        assertEquals(profile, ready.profile)
        assertEquals(context, ready.context)
    }

    @Test
    fun execute_contextResultIncomplete_returnsPreferenceSetupRequired() = runTest {
        val profile = createValidProfile()

        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { buildPersonalizedLearningContextWorkflow.execute(userId) } returns PersonalizedContextResult.Incomplete.InvalidLevelCombination()

        val result = workflow.execute(userId)

        assertTrue(result is UserStartupState.PreferenceSetupRequired)
        assertEquals(profile, (result as UserStartupState.PreferenceSetupRequired).profile)
    }

    @Test
    fun execute_profileWithEmptyTopicsResult_returnsPreferenceSetupRequired() = runTest {
        val profile = createValidProfile()

        coEvery { profileRepository.getUserProfile(userId) } returns profile
        coEvery { buildPersonalizedLearningContextWorkflow.execute(userId) } returns PersonalizedContextResult.Incomplete.EmptyPreferredTopics()

        val result = workflow.execute(userId)

        assertTrue(result is UserStartupState.PreferenceSetupRequired)
        assertEquals(profile, (result as UserStartupState.PreferenceSetupRequired).profile)
    }
}
