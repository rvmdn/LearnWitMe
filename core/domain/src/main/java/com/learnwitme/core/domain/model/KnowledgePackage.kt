package com.learnwitme.core.domain.model

enum class PackageStatus {
    DRAFT, PUBLISHED, DEPRECATED, ARCHIVED
}

/**
 * Responsibility: Serves as the Aggregate Root representing a bundle of educational materials
 * (Topics and KnowledgeCards) that users can acquire, complete, or reference.
 */
data class KnowledgePackage(
    override val id: KnowledgePackageId,
    val title: String,
    val description: String,
    val version: Int,
    val status: PackageStatus = PackageStatus.DRAFT,
    val topics: List<Topic> = emptyList(),
    val cards: List<KnowledgeCard> = emptyList(),
    val dependencies: List<KnowledgePackageId> = emptyList()
) : AggregateRoot<KnowledgePackageId> {

    init {
        require(title.isNotBlank()) { "Package title cannot be blank" }
        require(version > 0) { "Package version must be greater than zero" }

        // Duplicate Topic detection
        val topicCodes = topics.map { it.code }
        require(topicCodes.size == topicCodes.toSet().size) { "Package cannot contain duplicate Topics" }

        // Duplicate KnowledgeCard detection
        val cardIds = cards.map { it.id }
        require(cardIds.size == cardIds.toSet().size) { "Package cannot contain duplicate KnowledgeCards" }
    }

    /**
     * Publishes the package, validating consistency (must contain at least one topic and card).
     */
    fun publish(): KnowledgePackage {
        require(status == PackageStatus.DRAFT) { "Only DRAFT packages can be published (current: $status)" }
        require(topics.isNotEmpty()) { "Cannot publish a package without topics" }
        require(cards.isNotEmpty()) { "Cannot publish a package without knowledge cards" }

        return this.copy(status = PackageStatus.PUBLISHED)
    }

    /**
     * Deprecates a published package.
     */
    fun deprecate(): KnowledgePackage {
        require(status == PackageStatus.PUBLISHED) { "Only PUBLISHED packages can be deprecated (current: $status)" }
        return this.copy(status = PackageStatus.DEPRECATED)
    }

    /**
     * Archives a package, transition to a terminal state.
     */
    fun archive(): KnowledgePackage {
        require(status != PackageStatus.ARCHIVED) { "Package is already ARCHIVED" }
        return this.copy(status = PackageStatus.ARCHIVED)
    }

    /**
     * Adds a Topic to the package, enforcing archived and duplicate invariants.
     */
    fun addTopic(topic: Topic): KnowledgePackage {
        require(status != PackageStatus.ARCHIVED) { "Cannot modify an ARCHIVED package" }
        if (topics.any { it.code == topic.code }) {
            return this // Idempotent
        }
        return this.copy(topics = topics + topic)
    }

    /**
     * Adds a KnowledgeCard to the package, enforcing archived and duplicate invariants.
     */
    fun addCard(card: KnowledgeCard): KnowledgePackage {
        require(status != PackageStatus.ARCHIVED) { "Cannot modify an ARCHIVED package" }
        if (cards.any { it.id == card.id }) {
            return this // Idempotent
        }
        return this.copy(cards = cards + card)
    }

    /**
     * Adds a dependency to another package, enforcing archived invariants.
     */
    fun addDependency(dependencyId: KnowledgePackageId): KnowledgePackage {
        require(status != PackageStatus.ARCHIVED) { "Cannot modify an ARCHIVED package" }
        if (dependencies.contains(dependencyId)) {
            return this
        }
        return this.copy(dependencies = dependencies + dependencyId)
    }
}
