package com.learnwitme.core.domain.model

/**
 * Responsibility: Serves as the Aggregate Root managing the dynamic mastery state, active learning cards,
 * and evaluated skill competencies for a specific user.
 */
data class UserKnowledge(
    override val id: UserId,
    val competencies: Map<String, Competency> = emptyMap(),
    val activeCardIds: Set<KnowledgeCardId> = emptySet(),
    val masteredCardIds: Set<KnowledgeCardId> = emptySet()
) : AggregateRoot<UserId> {

    /**
     * Marks a KnowledgeCard as mastered, removing it from active cards if present.
     */
    fun markCardMastered(cardId: KnowledgeCardId): UserKnowledge {
        if (masteredCardIds.contains(cardId)) {
            return this
        }
        return this.copy(
            activeCardIds = activeCardIds - cardId,
            masteredCardIds = masteredCardIds + cardId
        )
    }

    /**
     * Updates or records a competency evaluation for a specific Skill.
     */
    fun updateCompetency(skill: Skill, newCompetency: Competency): UserKnowledge {
        val updated = competencies.toMutableMap()
        updated[skill.code] = newCompetency
        return this.copy(competencies = updated)
    }

    /**
     * Starts active learning on a KnowledgeCard.
     */
    fun startLearning(cardId: KnowledgeCardId): UserKnowledge {
        if (masteredCardIds.contains(cardId) || activeCardIds.contains(cardId)) {
            return this
        }
        return this.copy(activeCardIds = activeCardIds + cardId)
    }

    /**
     * Resets user progress across all cards and skill competencies.
     */
    fun resetProgress(): UserKnowledge {
        return this.copy(
            competencies = emptyMap(),
            activeCardIds = emptySet(),
            masteredCardIds = emptySet()
        )
    }
}
