package com.learnwitme.core.domain.model

data class KnowledgeNode(
    val cardId: KnowledgeCardId,
    val relatedTopicCodes: List<String>,
    val prerequisites: List<KnowledgeCardId>
) : ValueObject

enum class RelationType {
    PREREQUISITE,
    RELATED
}

data class KnowledgeRelation(
    val sourceId: KnowledgeCardId,
    val targetId: KnowledgeCardId,
    val type: RelationType
) : ValueObject

data class KnowledgeGraph(
    val nodes: Map<KnowledgeCardId, KnowledgeNode>,
    val edges: List<KnowledgeRelation>
) : ValueObject

data class LearningPath(
    val orderedCardIds: List<KnowledgeCardId>
) : ValueObject

data class KnowledgeAvailability(
    val isAvailable: Boolean,
    val unmetPrerequisites: List<KnowledgeCardId>
) : ValueObject

data class KnowledgePolicy(
    val maxRelatedTopics: Int = 5,
    val enforceStrictPrerequisites: Boolean = true
) : ValueObject

data class KnowledgeDecision(
    val graph: KnowledgeGraph,
    val learningPath: LearningPath,
    val availabilityMap: Map<KnowledgeCardId, KnowledgeAvailability>
) : ValueObject
