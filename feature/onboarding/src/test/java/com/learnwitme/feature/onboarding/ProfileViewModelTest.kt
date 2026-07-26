package com.learnwitme.feature.onboarding

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.usecase.LoadUserProfileWorkflow
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val loadWorkflow = mockk<LoadUserProfileWorkflow>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadProfileSuccess() = runTest(testDispatcher) {
        val userId = UserId("current_user")
        val profile = UserProfile(
            userId, 
            CareerPath("cp_dev", "Dev", ""), 
            LearningPreference(listOf("t"), 30), 
            DifficultyLevel.BEGINNER, 
            DifficultyLevel.BEGINNER, 
            LearningProgress(0, 0, 0, 0)
        )
        coEvery { loadWorkflow.execute(userId) } returns profile

        val viewModel = ProfileViewModel(loadWorkflow)
        
        assertTrue(viewModel.uiState.value is ProfileState.Loading)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is ProfileState.Success)
        assertEquals(profile, (viewModel.uiState.value as ProfileState.Success).profile)
    }
}
