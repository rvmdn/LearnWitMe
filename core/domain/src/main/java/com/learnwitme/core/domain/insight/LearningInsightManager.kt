package com.learnwitme.core.domain.insight

import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.LearningRepository

/**
 * Responsibility: Orchestrates candidate data collection, trend analysis, and explainable
 * insight summarization for learner feedback.
 */
class LearningInsightManager(
    private val learningRepository: LearningRepository,
    private val collector: InsightCollector = InsightCollector(),
    private val analyzer: InsightAnalyzer = InsightAnalyzer(),
    private val summarizer: InsightSummarizer = InsightSummarizer()
) {
    suspend fun generateInsight(
        userId: UserId,
        currentTimeMs: Long,
        policy: LearningInsightPolicy = LearningInsightPolicy()
    ): LearningInsight {
        val userKnowledge = learningRepository.findUserKnowledge(userId)
        val dueReviews = learningRepository.findDueReviews(userId, currentTimeMs)

        val data = collector.createCollectedData(userKnowledge, dueReviews, userId.value)
        val analysis = analyzer.analyze(data, policy, currentTimeMs)
        return summarizer.summarize(analysis)
    }
}
