package com.learnwitme.app.ui.onboarding

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPreference as MemoryLearningPreference
import com.learnwitme.core.domain.usecase.InitializeGuestUserWorkflow
import com.learnwitme.core.domain.usecase.UpdateLearningPreferenceWorkflow
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val initializeGuestUserWorkflow = mockk<InitializeGuestUserWorkflow>()
    private val updateLearningPreferenceWorkflow = mockk<UpdateLearningPreferenceWorkflow>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun continueAsGuest_success_updatesStateToPreferenceStep() = runTest(testDispatcher) {
        val guestProfile = UserProfile(
            id = UserId("current_user"),
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
        coEvery { initializeGuestUserWorkflow.execute() } returns guestProfile

        val viewModel = OnboardingViewModel(initializeGuestUserWorkflow, updateLearningPreferenceWorkflow)

        assertEquals(OnboardingState.Idle, viewModel.state.value)

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertEquals(OnboardingState.PreferenceStep, viewModel.state.value)
    }

    @Test
    fun continueAsGuest_failure_updatesStateToError() = runTest(testDispatcher) {
        coEvery { initializeGuestUserWorkflow.execute() } throws RuntimeException("Failed to persist guest profile")

        val viewModel = OnboardingViewModel(initializeGuestUserWorkflow, updateLearningPreferenceWorkflow)

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.Error)
        assertEquals("Failed to persist guest profile", (viewModel.state.value as OnboardingState.Error).message)
    }

    @Test
    fun savePreferences_success_updatesStateToSuccess() = runTest(testDispatcher) {
        val updatedProfile = UserProfile(
            id = UserId("current_user"),
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            preference = LearningPreference(listOf("tp_kotlin"), 45),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
        coEvery {
            updateLearningPreferenceWorkflow.execute(
                userId = any(),
                preferredTopics = any(),
                dailyLimitMinutes = any(),
                careerPath = any(),
                currentLevel = any(),
                targetLevel = any()
            )
        } returns updatedProfile

        val viewModel = OnboardingViewModel(initializeGuestUserWorkflow, updateLearningPreferenceWorkflow)

        viewModel.savePreferences(listOf("tp_kotlin"), 45, DifficultyLevel.MID)
        advanceUntilIdle()

        assertEquals(OnboardingState.Success, viewModel.state.value)
    }
}
