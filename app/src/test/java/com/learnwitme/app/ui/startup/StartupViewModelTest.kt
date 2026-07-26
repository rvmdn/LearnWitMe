package com.learnwitme.app.ui.startup

import android.content.Context
import android.content.res.AssetManager
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.usecase.InitializeApplicationWorkflow
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

import com.learnwitme.core.config.LocalizationManager
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val initWorkflow = mockk<InitializeApplicationWorkflow>(relaxed = true)
    private val profileRepo = mockk<UserProfileRepository>()
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

    @Test
    fun testInitialization_firstLaunch_requiresOnboarding() = runTest(testDispatcher) {
        val userId = UserId("current_user")
        
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

        coEvery { profileRepo.getUserProfile(userId) } returns null

        val viewModel = StartupViewModel(initWorkflow, profileRepo, localizationManager, context)
        
        assertTrue(viewModel.state.value is StartupState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.OnboardingRequired)
    }

    @Test
    fun testInitialization_returningUser_success() = runTest(testDispatcher) {
        val userId = UserId("current_user")
        
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

        val existingProfile = UserProfile(
            userId,
            CareerPath("cp_kotlin_dev", "Kotlin Developer", ""),
            LearningPreference(listOf("tp_kotlin"), 30),
            DifficultyLevel.BEGINNER,
            DifficultyLevel.BEGINNER,
            LearningProgress(0, 0, 0, 0L)
        )
        coEvery { profileRepo.getUserProfile(userId) } returns existingProfile

        val viewModel = StartupViewModel(initWorkflow, profileRepo, localizationManager, context)
        
        assertTrue(viewModel.state.value is StartupState.Loading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value is StartupState.Success)
    }
}
