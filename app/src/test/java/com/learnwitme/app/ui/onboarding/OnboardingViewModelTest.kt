package com.learnwitme.app.ui.onboarding

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.DailyInitializationState
import com.learnwitme.core.domain.model.memory.personalization.DailyLearningInitializationResult
import com.learnwitme.core.domain.model.memory.personalization.PersonalizedContextResult
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.usecase.InitializeGuestUserWorkflow
import com.learnwitme.core.domain.usecase.InitializePersonalizedDailyLearningWorkflow
import com.learnwitme.core.domain.usecase.UpdateLearningPreferenceWorkflow
import io.mockk.coEvery
import io.mockk.every
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
    private val initializePersonalizedDailyLearningWorkflow = mockk<InitializePersonalizedDailyLearningWorkflow>()
    private val profileRepository = mockk<UserProfileRepository>()

    private val userId = UserIdentity.Guest.DEFAULT_GUEST_ID

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { profileRepository.getUserProfile(any()) } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            initializeGuestUserWorkflow,
            updateLearningPreferenceWorkflow,
            initializePersonalizedDailyLearningWorkflow,
            profileRepository
        )
    }

    @Test
    fun init_freshLaunch_stateIsIdle() = runTest(testDispatcher) {
        coEvery { profileRepository.getUserProfile(userId) } returns null

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(OnboardingState.Idle, viewModel.state.value)
    }

    @Test
    fun init_profileExistsWithEmptyPreferences_advancesToPreferenceStep() = runTest(testDispatcher) {
        val existingGuestProfile = mockk<UserProfile>()
        every { existingGuestProfile.preference.preferredTopics } returns emptyList()
        coEvery { profileRepository.getUserProfile(userId) } returns existingGuestProfile

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.PreferenceStep)
    }

    @Test
    fun continueAsGuest_success_updatesStateToPreferenceStep() = runTest(testDispatcher) {
        val guestProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Desc"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.MID,
            progress = LearningProgress(0, 0, 0, 0L)
        )
        coEvery { initializeGuestUserWorkflow.execute() } returns guestProfile

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(OnboardingState.Idle, viewModel.state.value)

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.PreferenceStep)
    }

    @Test
    fun continueAsGuest_failure_updatesStateToError() = runTest(testDispatcher) {
        coEvery { initializeGuestUserWorkflow.execute() } throws RuntimeException("Failed to persist guest profile")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.continueAsGuest()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.Error)
        val errorState = viewModel.state.value as OnboardingState.Error
        assertEquals("Failed to persist guest profile", errorState.message)
        assertEquals(false, errorState.canRetryPreferences)
    }

    @Test
    fun savePreferences_validationFailure_remainsInPreferenceStepWithError() = runTest(testDispatcher) {
        coEvery {
            updateLearningPreferenceWorkflow.execute(
                userId = any(),
                preferredTopics = any(),
                dailyLimitMinutes = any(),
                careerPath = any(),
                currentLevel = any(),
                targetLevel = any()
            )
        } throws IllegalArgumentException("Daily learning limit must be positive")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.savePreferences(listOf("tp_kotlin"), -10, DifficultyLevel.MID)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.PreferenceStep)
        val prefState = viewModel.state.value as OnboardingState.PreferenceStep
        assertEquals("Daily learning limit must be positive", prefState.error)
    }

    @Test
    fun savePreferences_success_triggersPersonalizationAndSetsSuccess() = runTest(testDispatcher) {
        val updatedProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Desc"),
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

        val successResult = DailyLearningInitializationResult.Success(
            userId = userId,
            context = mockk(relaxed = true),
            feedDecision = mockk(relaxed = true),
            initializationState = DailyInitializationState.NEW_GUEST_INITIALIZED
        )
        coEvery { initializePersonalizedDailyLearningWorkflow.execute(userId) } returns successResult

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.savePreferences(listOf("tp_kotlin"), 45, DifficultyLevel.MID)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.Success)
        assertEquals(successResult, (viewModel.state.value as OnboardingState.Success).initializationResult)
    }

    @Test
    fun savePreferences_personalizationIncomplete_remainsInPreferenceStepWithError() = runTest(testDispatcher) {
        val updatedProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Desc"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
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

        val incompleteResult = DailyLearningInitializationResult.IncompletePersonalization(
            PersonalizedContextResult.Incomplete.EmptyPreferredTopics()
        )
        coEvery { initializePersonalizedDailyLearningWorkflow.execute(userId) } returns incompleteResult

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.savePreferences(listOf("tp_kotlin"), 30, DifficultyLevel.MID)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.PreferenceStep)
        val prefState = viewModel.state.value as OnboardingState.PreferenceStep
        assertTrue(prefState.error?.contains("Personalization incomplete") == true)
    }

    @Test
    fun savePreferences_contentUnavailable_setsErrorWithRetry() = runTest(testDispatcher) {
        val updatedProfile = UserProfile(
            id = userId,
            careerPath = CareerPath("cp_kotlin_dev", "Kotlin Developer", "Desc"),
            preference = LearningPreference(listOf("tp_kotlin"), 30),
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

        val unavailableResult = DailyLearningInitializationResult.ContentUnavailable(
            userId = userId,
            context = mockk(relaxed = true),
            reason = "No active roadmap found"
        )
        coEvery { initializePersonalizedDailyLearningWorkflow.execute(userId) } returns unavailableResult

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.savePreferences(listOf("tp_kotlin"), 30, DifficultyLevel.MID)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is OnboardingState.Error)
        val errorState = viewModel.state.value as OnboardingState.Error
        assertTrue(errorState.message.contains("Learning content unavailable"))
        assertTrue(errorState.canRetryPreferences)
    }
}
