package com.learnwitme.core.domain.adaptive

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackageId
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationCollectorTest {

    private val collector = RecommendationCollector()

    private fun createCard(id: String) = KnowledgeCard(
        id = KnowledgeCardId(id),
        packageId = KnowledgePackageId("pkg_1"),
        content = KnowledgeContent("Title $id", "Sum", "Why", "Ex", "Mis", "Not", "Best")
    )

    @Test
    fun testCollectCandidatesCategorization() {
        val cardDue = createCard("card_due")
        val cardWeak = createCard("card_weak")
        val cardNew = createCard("card_new")
        val cardChallenge = createCard("card_challenge")

        val pool = collector.collectCandidates(
            availableCards = listOf(cardDue, cardWeak, cardNew, cardChallenge),
            dueCardIds = setOf("card_due"),
            activeCardIds = setOf("card_weak"),
            masteredCardIds = setOf("card_challenge"),
            targetDifficulty = AdaptiveDifficultyLevel.MEDIUM
        )

        assertEquals(1, pool.dueCards.size)
        assertEquals("card_due", pool.dueCards.first().id.value)

        assertEquals(1, pool.weakSkillCards.size)
        assertEquals("card_weak", pool.weakSkillCards.first().id.value)

        assertEquals(1, pool.newTopicCards.size)
        assertEquals("card_new", pool.newTopicCards.first().id.value)

        assertEquals(1, pool.challengeCards.size)
        assertEquals("card_challenge", pool.challengeCards.first().id.value)
    }
}
