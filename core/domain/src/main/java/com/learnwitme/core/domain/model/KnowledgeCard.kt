package com.learnwitme.core.domain.model

/**
 * Responsibility: Represents an atomic unit of knowledge containing rich explanations,
 * real-world context, best practices, reference materials, and metadata.
 */
data class KnowledgeCard(
    override val id: KnowledgeCardId,
    val title: String,
    val summary: String,
    val whyItMatters: String,
    val realWorldExample: String,
    val commonMistakes: String,
    val whenNotToUse: String,
    val bestPractice: String,
    val reference: List<Reference>,
    val officialDocumentationLink: String,
    val difficulty: DifficultyLevel,
    val estimatedReadingTimeSeconds: Int,
    val careerImpact: String,
    val tags: List<Tag>,
    val prerequisiteCardIds: List<KnowledgeCardId>,
    val relatedTopicCodes: List<String>
) : Entity<KnowledgeCardId> {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(summary.isNotBlank()) { "Summary cannot be blank" }
        require(whyItMatters.isNotBlank()) { "Why It Matters explanation cannot be blank" }
        require(estimatedReadingTimeSeconds > 0) { "Estimated reading time must be greater than zero" }
    }
}
