package com.learnwitme.core.model

import kotlinx.serialization.Serializable
import com.learnwitme.core.domain.model.DifficultyLevel

@Serializable
data class KnowledgeCard(
    val id: String,
    val title: String,
    val summary: String,
    val whyItMatters: String,
    val realWorldExample: String,
    val commonMistakes: String,
    val whenNotToUse: String,
    val bestPractice: String,
    val reference: List<String>,
    val officialDocumentationLink: String,
    val difficulty: DifficultyLevel,
    val estimatedReadingTimeSeconds: Int,
    val careerImpact: String,
    val tags: List<String>,
    val prerequisiteCardIds: List<String>,
    val relatedTopicIds: List<String>
)
