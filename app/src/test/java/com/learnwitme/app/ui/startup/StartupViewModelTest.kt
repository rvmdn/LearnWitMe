package com.learnwitme.app.ui.startup

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.core.config.LocalizationManager
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext
import com.learnwitme.core.domain.usecase.DetermineUserStartupStateWorkflow
import com.learnwitme.core.domain.usecase.InitializeApplicationWorkflow
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val initWorkflow = mockk<InitializeApplicationWorkflow>(relaxed = true)
    private val determineStateWorkflow = mockk<DetermineUserStartupStateWorkflow>()
    private val localizationManager = mockk<LocalizationManager>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { localizationManager.currentLanguage } returns MutableStateFlow("id")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupAssetManager() {
        val assets = mockk<AssetManager>()
        every { context.assets } returns assets
        val fakeJson = """
            {
              "metadata": {
                "id": "p1",
                "title": "T",
                "description": "D",
                "version": { "major": 1, "minor": 0, "patch": 0 },
                "checksum": "chk"
              },
              "topics": [],
              "cards": [],
              "units": [],
              "quizzes": [],
              "dependencies": []
            }
        """.trimIndent()
        every { assets.open(any()) } returns ByteArrayInputStream(fakeJson.toByteArray())
    }

    @Test
    fun testInitialization_firstLaunch_requiresNewGuestOnboarding() = runTest(testDispatcher) {
        val userId = UserIdentity.Guest.DEFAULT_GUEST_ID
        setupAssetManager()

        coEvery { determineStateWorkflow.execute(userId) } returns UserStartupState.NewGuest(userId)

        val viewModel = StartupViewModel(initWorkflow, determineStateWorkflow, localizationManager, context)

        assertTrue(viewModel.state.value is StartupState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.NewGuestOnboarding)
    }

    @Test
    fun testInitialization_profileWithoutPreferences_requiresPreferenceSetup() = runTest(testDispatcher) {
        val userId = UserIdentity.Guest.DEFAULT_GUEST_ID
        setupAssetManager()

        val profile = mockk<UserProfile>()
        coEvery { determineStateWorkflow.execute(userId) } returns UserStartupState.PreferenceSetupRequired(profile)

        val viewModel = StartupViewModel(initWorkflow, determineStateWorkflow, localizationManager, context)

        assertTrue(viewModel.state.value is StartupState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.PreferenceSetupRequired)
    }

    @Test
    fun testInitialization_returningUser_ready() = runTest(testDispatcher) {
        val userId = UserIdentity.Guest.DEFAULT_GUEST_ID
        setupAssetManager()

        val existingProfile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", "Desc"),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.BEGINNER,
            LearningProgress(0, 0, 0, 0L)
        )
        val contextMock = mockk<LearningPersonalizationContext>(relaxed = true)
        coEvery { determineStateWorkflow.execute(userId) } returns UserStartupState.Ready(existingProfile, contextMock)

        val viewModel = StartupViewModel(initWorkflow, determineStateWorkflow, localizationManager, context)

        assertTrue(viewModel.state.value is StartupState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.Ready)
    }

    @Test
    fun testInitialization_error_emitsErrorState() = runTest(testDispatcher) {
        val userId = UserIdentity.Guest.DEFAULT_GUEST_ID
        val assets = mockk<AssetManager>()
        every { context.assets } returns assets
        every { assets.open(any()) } throws RuntimeException("Corrupted package asset")

        val viewModel = StartupViewModel(initWorkflow, determineStateWorkflow, localizationManager, context)

        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.Error)
    }
}
