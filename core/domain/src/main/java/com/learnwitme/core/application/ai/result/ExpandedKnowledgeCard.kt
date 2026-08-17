package com.learnwitme.core.application.ai.result

import com.learnwitme.core.domain.model.KnowledgeCard

data class ExpandedKnowledgeCard(
    val originalCard: KnowledgeCard,
    val expandedContent: String,
    val examples: List<String> = emptyList(),
    val relatedTopics: List<String> = emptyList()
)
