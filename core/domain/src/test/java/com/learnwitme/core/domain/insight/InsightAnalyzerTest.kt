package com.learnwitme.core.domain.insight

import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightAnalyzerTest {

    private val analyzer = InsightAnalyzer()

    @Test
    fun testAnalyzeReviewOverdueTrend() {
        val userId = UserId("usr_1")
        val due = (1..6).map { ReviewSchedule(KnowledgeCardId("c_$it"), intervalDays = 1, nextReviewTimestamp = 500L, easeFactor = 2.5, repetitions = 1) }
        val data = InsightCollector.CollectedData(UserKnowledge(id = userId), due)

        val result = analyzer.analyze(data, LearningInsightPolicy(), currentTimeMs = 1000L)
        assertEquals(LearningTrend.REVIEW_OVERDUE, result.trend)
    }

    @Test
    fun testAnalyzeMasteryGrowthTrend() {
        val userId = UserId("usr_2")
        val mastered = (1..5).map { KnowledgeCardId("cm_$it") }.toSet()
        val active = setOf(KnowledgeCardId("ca_1"))
        val data = InsightCollector.CollectedData(UserKnowledge(id = userId, activeCardIds = active, masteredCardIds = mastered), emptyList())

        val result = analyzer.analyze(data, LearningInsightPolicy(), currentTimeMs = 1000L)
        assertEquals(LearningTrend.MASTERY_GROWTH, result.trend)
    }

    @Test
    fun testAnalyzeLongInactivityTrend() {
        val userId = UserId("usr_3")
        val data = InsightCollector.CollectedData(UserKnowledge(id = userId), emptyList())

        val result = analyzer.analyze(data, LearningInsightPolicy(), currentTimeMs = 1000L)
        assertEquals(LearningTrend.LONG_INACTIVITY, result.trend)
    }
}
