package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.storage.source.LearnWitMeLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryImplTests {
    
    private val localDataSource = mockk<LearnWitMeLocalDataSource>(relaxed = true)
    
    @Test
    fun testUserProfileRepository() = runBlocking {
        val repo = UserProfileRepositoryImpl(localDataSource)
        val userId = UserId("u1")
        val profile = UserProfile(userId, CareerPath("cp_dev", "Dev", ""), LearningPreference(listOf("t"), 30), DifficultyLevel.BEGINNER, DifficultyLevel.BEGINNER, LearningProgress(0,0,0,0L))
        
        coEvery { localDataSource.getUserProfile(userId) } returns profile
        
        val result = repo.getUserProfile(userId)
        assertEquals(profile, result)
        
        repo.saveUserProfile(profile)
        coVerify { localDataSource.saveUserProfile(profile) }
    }

    @Test
    fun testLearningMemoryRepository() = runBlocking {
        val repo = LearningMemoryRepositoryImpl(localDataSource)
        val userId = UserId("u1")
        val memory = LearningMemory(LearningMemoryId("m1"), userId, emptyList())
        
        coEvery { localDataSource.getLearningMemory(userId) } returns memory
        
        val result = repo.getMemoryByUserId(userId)
        assertEquals(memory, result)
        
        repo.saveMemory(memory)
        coVerify { localDataSource.saveLearningMemory(memory) }
    }
    
    @Test
    fun testProgressRepositoryNullHandling() = runBlocking {
        val repo = ProgressRepositoryImpl(localDataSource)
        val userId = UserId("u1")
        
        // When profile is null
        coEvery { localDataSource.getUserProfile(userId) } returns null
        
        val result = repo.getProgress(userId)
        assertEquals(null, result)
        
        // saveProgress should not call saveUserProfile if profile is null
        repo.saveProgress(userId, LearningProgress(1,1,1,1L))
        coVerify(exactly = 0) { localDataSource.saveUserProfile(any()) }
    }
}
