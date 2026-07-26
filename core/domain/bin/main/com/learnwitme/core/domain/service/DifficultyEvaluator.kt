package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.UserKnowledge

/**
 * Responsibility: Pure, stateless domain service evaluating the effective cognitive difficulty
 * of a KnowledgeCard for a specific learner based on their current UserKnowledge mastery.
 */
class DifficultyEvaluator {

    /**
     * Evaluates effective difficulty for a card, shifting difficulty higher if prerequisites are unmet,
     * or shifting difficulty lower if the user has high skill competency.
     */
    fun evaluateEffectiveDifficulty(card: KnowledgeCard, userKnowledge: UserKnowledge): DifficultyLevel {
        val unmetPrerequisites = card.prerequisiteCardIds.filter { !userKnowledge.masteredCardIds.contains(it) }

        // If prerequisites are unmet, elevate difficulty by 1 level if possible
        if (unmetPrerequisites.isNotEmpty()) {
            val levels = DifficultyLevel.entries.toTypedArray()
            val nextIndex = (card.difficulty.ordinal + 1).coerceAtMost(levels.lastIndex)
            return levels[nextIndex]
        }

        // Check competency across skills required by card
        val totalScore = card.skills.sumOf { skill ->
            userKnowledge.competencies[skill.code]?.score ?: 0.0
        }
        val avgScore = if (card.skills.isNotEmpty()) totalScore / card.skills.size else 0.0

        // High competency lowers cognitive burden by 1 level if possible
        return if (avgScore >= 0.8 && card.difficulty != DifficultyLevel.BEGINNER) {
            val levels = DifficultyLevel.entries.toTypedArray()
            val prevIndex = (card.difficulty.ordinal - 1).coerceAtLeast(0)
            levels[prevIndex]
        } else {
            card.difficulty
        }
    }

    /**
     * Checks if a card's prerequisite dependency is satisfied by the user's mastered cards.
     */
    fun isPrerequisiteSatisfied(prerequisiteId: KnowledgeCardId, userKnowledge: UserKnowledge): Boolean {
        return userKnowledge.masteredCardIds.contains(prerequisiteId)
    }
}
