package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.CompetencyLevel
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.storage.local.dao.ReviewScheduleDao
import com.learnwitme.core.storage.local.dao.UserKnowledgeDao
import com.learnwitme.core.storage.local.entity.ReviewScheduleEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningRepositoryIntegrationTest {

    private val userKnowledgeDao = mockk<UserKnowledgeDao>(relaxed = true)
    private val reviewScheduleDao = mockk<ReviewScheduleDao>(relaxed = true)
    private val repository = LearningRepositoryImpl(userKnowledgeDao, reviewScheduleDao)

    @Test
    fun testStartLearning() = runBlocking {
        val userId = UserId("user_101")
        val cardId = KnowledgeCardId("card_101")

        coEvery { userKnowledgeDao.findByUserId("user_101") } returns null

        repository.startLearning(userId, cardId)

        coVerify { userKnowledgeDao.save(match { it.activeCardIdsJson.contains("card_101") }) }
    }

    @Test
    fun testRecordReview() = runBlocking {
        val userId = UserId("user_101")
        val cardId = KnowledgeCardId("card_101")
        val schedule = ReviewSchedule(cardId, intervalDays = 6, nextReviewTimestamp = 1000000L, easeFactor = 2.5, repetitions = 2)

        repository.recordReview(userId, cardId, schedule)

        coVerify { reviewScheduleDao.save(match { it.userId == "user_101" && it.cardId == "card_101" && it.intervalDays == 6 }) }
    }

    @Test
    fun testUpdateCompetency() = runBlocking {
        val userId = UserId("user_101")
        val skill = Skill("sk_kotlin", "Kotlin")
        val competency = Competency(CompetencyLevel.PROFICIENT, 0.85)

        coEvery { userKnowledgeDao.findByUserId("user_101") } returns null

        repository.updateCompetency(userId, skill, competency)

        coVerify { userKnowledgeDao.save(match { it.competenciesJson.contains("PROFICIENT") }) }
    }

    @Test
    fun testFindDueReviews() = runBlocking {
        val userId = UserId("user_101")
        val currentTimeMs = 1500000L
        val scheduleEntity = ReviewScheduleEntity("user_101", "card_101", 1, 1000000L, 2.5, 1)

        coEvery { reviewScheduleDao.findDueReviews("user_101", currentTimeMs) } returns listOf(scheduleEntity)

        val dueReviews = repository.findDueReviews(userId, currentTimeMs)

        assertEquals(1, dueReviews.size)
        assertEquals("card_101", dueReviews.first().cardId.value)
    }
}
