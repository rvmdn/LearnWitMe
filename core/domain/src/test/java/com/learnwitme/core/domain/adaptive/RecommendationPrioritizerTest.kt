package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationPrioritizerTest {

    private val prioritizer = RecommendationPrioritizer()

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title $id", "Sum", "Why", "Ex", "Mis", "Not", "Best")
    )

    @Test
    fun testPriorityOrderingAndDeduplication() {
        val cLow = createCard("c_low")
        val cCritical = createCard("c_critical")

        val recLow = LearningRecommendation(cLow, RecommendationReason.CHALLENGE, RecommendationPriority.LOW, 1.0, "Low")
        val recCritical = LearningRecommendation(cCritical, RecommendationReason.REVIEW_DUE, RecommendationPriority.CRITICAL, 4.0, "Crit")
        val recDup = LearningRecommendation(cCritical, RecommendationReason.REVIEW_DUE, RecommendationPriority.CRITICAL, 4.0, "Crit Dup")

        val result = prioritizer.prioritizeAndFilter(listOf(recLow, recCritical, recDup), RecommendationPolicy(maxRecommendations = 5))

        assertEquals(2, result.size)
        assertEquals("c_critical", result[0].card.id.value)
        assertEquals("c_low", result[1].card.id.value)
    }
}
