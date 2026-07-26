package com.learnwitme.core.application.service

import com.learnwitme.core.domain.adaptive.AdaptiveDifficultyLevel
import com.learnwitme.core.domain.adaptive.DifficultyAdjustmentReason
import com.learnwitme.core.domain.adaptive.DifficultyRecommendation
import com.learnwitme.core.domain.adaptive.LearningRecommendation
import com.learnwitme.core.domain.adaptive.RecommendationPriority
import com.learnwitme.core.domain.adaptive.RecommendationReason
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIProviderRegistry
import com.learnwitme.core.domain.ai.AIProviderRouter
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.ai.MockAIProvider
import com.learnwitme.core.domain.insight.LearningInsight
import com.learnwitme.core.domain.insight.LearningTrend
import com.learnwitme.core.domain.insight.WeeklyLearningSummary
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AIExplanationServiceTest {

    private val registry = AIProviderRegistry()
    private val router = AIProviderRouter(registry)
    private val aiManager = AIManager(router)
    private val explanationService = AIExplanationService(aiManager)

    @Before
    fun setUp() {
        registry.registerProvider(AIProviderType.MOCK, MockAIProvider())
    }

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title $id", "Summary", "Why", "Example", "Mistake", "Not", "Best")
    )

    @Test
    fun testEnhanceInsight() = runBlocking {
        val insight = LearningInsight(
            trend = LearningTrend.UPWARD_TREND,
            title = "Great Momentum!",
            description = "Accuracy is high.",
            strongestSkills = emptyList(),
            weakestSkills = emptyList(),
            weeklySummary = WeeklyLearningSummary(5, 5, 3, 0.90)
        )

        val enhanced = explanationService.enhanceInsight(insight)
        assertNotNull(enhanced)
        assertEquals(insight.trend, enhanced.trend)
        assertEquals(insight.title, enhanced.title)
        assertTrue(enhanced.description.contains("AI Insight Note:"))
    }

    @Test
    fun testExplainRecommendation() = runBlocking {
        val rec = LearningRecommendation(
            card = createCard("card_1"),
            reason = RecommendationReason.REVIEW_DUE,
            priority = RecommendationPriority.CRITICAL,
            score = 4.0,
            explanation = "Due"
        )

        val explanation = explanationService.explainRecommendation(rec)
        assertNotNull(explanation)
        assertTrue(explanation.contains("Explanation for:"))
    }

    @Test
    fun testExplainDifficultyChange() = runBlocking {
        val diffRec = DifficultyRecommendation(
            targetLevel = AdaptiveDifficultyLevel.MEDIUM,
            reason = DifficultyAdjustmentReason.HIGH_ACCURACY,
            explanation = "Promoted"
        )

        val explanation = explanationService.explainDifficultyChange(diffRec)
        assertNotNull(explanation)
        assertTrue(explanation.contains("Explanation for:"))
    }
}
