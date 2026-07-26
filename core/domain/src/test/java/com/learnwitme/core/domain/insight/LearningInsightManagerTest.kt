package com.learnwitme.core.domain.insight

import com.learnwitme.core.domain.adaptive.FakeAdaptiveLearningRepository
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LearningInsightManagerTest {

    private val repository = FakeAdaptiveLearningRepository()
    private val manager = LearningInsightManager(repository)
    private val userId = UserId("usr_ins_mgr_1")

    @Test
    fun testEndToEndInsightGeneration() = runBlocking {
        repository.userKnowledgeMap[userId.value] = UserKnowledge(
            id = userId,
            activeCardIds = setOf(KnowledgeCardId("c1")),
            masteredCardIds = setOf(KnowledgeCardId("c2"), KnowledgeCardId("c3"))
        )

        val insight = manager.generateInsight(userId, currentTimeMs = 1000L)
        assertNotNull(insight)
        assertEquals(LearningTrend.MASTERY_GROWTH, insight.trend)
        assertEquals("Mastery Acceleration", insight.title)
    }

    @Test
    fun testDeterministicInsightGeneration() = runBlocking {
        val ins1 = manager.generateInsight(userId, currentTimeMs = 1000L)
        val ins2 = manager.generateInsight(userId, currentTimeMs = 1000L)

        assertEquals(ins1, ins2)
    }
}
