package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationEvaluatorTest {

    private val evaluator = RecommendationEvaluator()

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title $id", "Sum", "Why", "Ex", "Mis", "Not", "Best")
    )

    @Test
    fun testEvaluateCandidatesPriorityAndReason() {
        val card1 = createCard("c1")
        val candidates = RecommendationCollector.CandidatePool(
            dueCards = listOf(card1),
            weakSkillCards = emptyList(),
            newTopicCards = emptyList(),
            challengeCards = emptyList()
        )

        val diffRec = DifficultyRecommendation(AdaptiveDifficultyLevel.MEDIUM, DifficultyAdjustmentReason.CONSISTENT_PROGRESS, "Ok")
        val recs = evaluator.evaluateCandidates(candidates, diffRec)

        assertEquals(1, recs.size)
        assertEquals(RecommendationPriority.CRITICAL, recs.first().priority)
        assertEquals(RecommendationReason.REVIEW_DUE, recs.first().reason)
    }
}
