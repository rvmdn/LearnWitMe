package com.learnwitme.core.domain.insight

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class InsightCollectorTest {

    private val collector = InsightCollector()

    @Test
    fun testCollectDataWithValues() {
        val userId = UserId("usr_ins_1")
        val uk = UserKnowledge(id = userId, activeCardIds = setOf(KnowledgeCardId("c1")))
        val due = listOf(ReviewSchedule(KnowledgeCardId("c1"), intervalDays = 1, nextReviewTimestamp = 500L, easeFactor = 2.5, repetitions = 1))

        val data = collector.createCollectedData(uk, due, userId.value)
        assertNotNull(data)
        assertEquals(1, data.userKnowledge.activeCardIds.size)
        assertEquals(1, data.dueReviews.size)
    }

    @Test
    fun testCollectDataWithNullUserKnowledge() {
        val data = collector.createCollectedData(null, emptyList(), "usr_null")
        assertNotNull(data)
        assertEquals(0, data.userKnowledge.activeCardIds.size)
    }
}
