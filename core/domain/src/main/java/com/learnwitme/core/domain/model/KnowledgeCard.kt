package com.learnwitme.core.domain.model

/**
 * Responsibility: Serves as the Aggregate Root for an atomic unit of learning material,
 * containing structured content, associated skills, reading metadata, and prerequisite dependency links.
 */
data class KnowledgeCard(
    override val id: KnowledgeCardId,
    val packageId: KnowledgePackageId = KnowledgePackageId("pkg_default"),
    val content: KnowledgeContent,
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val estimatedReadingTimeSeconds: Int = 120,
    val careerImpact: String = "MEDIUM",
    val skills: List<Skill> = emptyList(),
    val references: List<Reference> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val prerequisiteCardIds: List<KnowledgeCardId> = emptyList(),
    val relatedTopicCodes: List<String> = emptyList()
) : AggregateRoot<KnowledgeCardId> {

    val title: String get() = content.title
    val summary: String get() = content.summary
    val whyItMatters: String get() = content.whyItMatters
    val realWorldExample: String get() = content.realWorldExample
    val commonMistakes: String get() = content.commonMistakes
    val whenNotToUse: String get() = content.whenNotToUse
    val bestPractice: String get() = content.bestPractice
    val officialDocumentationLink: String get() = content.officialDocumentationLink
    val learningObjective: String get() = content.learningObjective

    constructor(
        id: KnowledgeCardId,
        packageId: KnowledgePackageId,
        title: String,
        summary: String,
        whyItMatters: String,
        realWorldExample: String,
        commonMistakes: String,
        whenNotToUse: String,
        bestPractice: String,
        reference: List<Reference>,
        officialDocumentationLink: String,
        difficulty: DifficultyLevel,
        estimatedReadingTimeSeconds: Int,
        careerImpact: String,
        tags: List<Tag>,
        prerequisiteCardIds: List<KnowledgeCardId>,
        relatedTopicCodes: List<String>,
        learningObjective: String = ""
    ) : this(
        id = id,
        packageId = packageId,
        content = KnowledgeContent(
            title = title,
            summary = summary,
            whyItMatters = whyItMatters,
            realWorldExample = realWorldExample,
            commonMistakes = commonMistakes,
            whenNotToUse = whenNotToUse,
            bestPractice = bestPractice,
            officialDocumentationLink = officialDocumentationLink,
            learningObjective = learningObjective
        ),
        difficulty = difficulty,
        estimatedReadingTimeSeconds = estimatedReadingTimeSeconds,
        careerImpact = careerImpact,
        skills = emptyList(),
        references = reference,
        tags = tags,
        prerequisiteCardIds = prerequisiteCardIds,
        relatedTopicCodes = relatedTopicCodes
    )

    init {
        require(estimatedReadingTimeSeconds > 0) { "Estimated reading time must be greater than zero" }
        require(!prerequisiteCardIds.contains(id)) { "Card $id cannot have itself as a prerequisite" }

        val prerequisiteSet = prerequisiteCardIds.toSet()
        require(prerequisiteSet.size == prerequisiteCardIds.size) { "Prerequisite list cannot contain duplicate card IDs" }

        val skillCodes = skills.map { it.code }
        require(skillCodes.size == skillCodes.toSet().size) { "Card cannot contain duplicate skills" }
    }

    /**
     * Adds a prerequisite card dependency, enforcing non-self reference and non-duplicate invariants.
     */
    fun addPrerequisite(prerequisiteId: KnowledgeCardId): KnowledgeCard {
        require(prerequisiteId != id) { "Cannot add card itself ($id) as a prerequisite" }
        if (prerequisiteCardIds.contains(prerequisiteId)) {
            return this // Idempotent
        }
        return this.copy(prerequisiteCardIds = prerequisiteCardIds + prerequisiteId)
    }

    /**
     * Removes a prerequisite card dependency.
     */
    fun removePrerequisite(prerequisiteId: KnowledgeCardId): KnowledgeCard {
        if (!prerequisiteCardIds.contains(prerequisiteId)) {
            return this
        }
        return this.copy(prerequisiteCardIds = prerequisiteCardIds - prerequisiteId)
    }

    /**
     * Updates the card content payload.
     */
    fun updateContent(newContent: KnowledgeContent): KnowledgeCard {
        return this.copy(content = newContent)
    }

    /**
     * Adds a skill tag to the card, enforcing uniqueness by skill code.
     */
    fun addSkill(skill: Skill): KnowledgeCard {
        if (skills.any { it.code == skill.code }) {
            return this
        }
        return this.copy(skills = skills + skill)
    }

    /**
     * Removes a skill by its unique skill code.
     */
    fun removeSkill(skillCode: String): KnowledgeCard {
        return this.copy(skills = skills.filter { it.code != skillCode })
    }
}
