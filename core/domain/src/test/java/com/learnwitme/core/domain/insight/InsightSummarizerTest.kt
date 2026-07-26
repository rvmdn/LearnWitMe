package com.learnwitme.core.domain.insight

import org.junit.Assert.assertEquals
import org.junit.Test

class InsightSummarizerTest {

    private val summarizer = InsightSummarizer()

    @Test
    fun testSummarizeUpwardTrend() {
        val analysis = InsightAnalyzer.AnalysisResult(
            trend = LearningTrend.UPWARD_TREND,
            strongestSkills = emptyList(),
            weakestSkills = emptyList(),
            weeklySummary = WeeklyLearningSummary(10, 10, 5, 0.90)
        )

        val insight = summarizer.summarize(analysis)
        assertEquals("Great Momentum!", insight.title)
        assertEquals(LearningTrend.UPWARD_TREND, insight.trend)
    }
}
