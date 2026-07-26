package com.learnwitme.core.domain.event

import com.learnwitme.core.domain.model.CompetencyLevel
import com.learnwitme.core.domain.model.DifficultyLevel

/**
 * Responsibility: Marker interface for all immutable domain events in the system.
 * Captures historical facts about state transitions within aggregate boundaries.
 */
interface DomainEvent {
    val eventId: String
    val occurredAt: Long
    val aggregateId: String
    val aggregateType: String
}

// --- Knowledge Events ---

/**
 * Emitted when a KnowledgePackage has been imported into the domain repository.
 */
data class KnowledgePackageImportedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "KnowledgePackage",
    val version: Int,
    val totalCardsImported: Int,
    val totalTopicsImported: Int
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
    }
}

/**
 * Emitted when a new KnowledgeCard is authored or registered.
 */
data class KnowledgeCardCreatedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "KnowledgeCard",
    val packageId: String,
    val title: String,
    val difficulty: DifficultyLevel
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
    }
}

/**
 * Emitted when content or metadata of a KnowledgeCard is modified.
 */
data class KnowledgeCardUpdatedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "KnowledgeCard",
    val newTitle: String,
    val difficulty: DifficultyLevel
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
    }
}

// --- Learning Events ---

/**
 * Emitted when a user initiates active learning on a KnowledgeCard.
 */
data class LearningStartedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "UserKnowledge",
    val cardId: String
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
        require(cardId.isNotBlank()) { "cardId cannot be blank" }
    }
}

/**
 * Emitted when a user completes a spaced-repetition card review session.
 */
data class ReviewCompletedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "ReviewSchedule",
    val cardId: String,
    val qualityScore: Double,
    val nextReviewTimestamp: Long
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
        require(qualityScore in 0.0..1.0) { "qualityScore must be bounded between 0.0 and 1.0" }
    }
}

/**
 * Emitted when a user achieves full mastery of a KnowledgeCard.
 */
data class CardMasteredEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "UserKnowledge",
    val cardId: String
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
        require(cardId.isNotBlank()) { "cardId cannot be blank" }
    }
}

/**
 * Emitted when a user's competency evaluation level changes for a specific Skill.
 */
data class CompetencyUpdatedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "UserKnowledge",
    val skillCode: String,
    val competencyLevel: CompetencyLevel,
    val score: Double
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
        require(skillCode.isNotBlank()) { "skillCode cannot be blank" }
        require(score in 0.0..1.0) { "score must be bounded between 0.0 and 1.0" }
    }
}

// --- Recommendation Events ---

/**
 * Emitted when the RecommendationEngine builds a fresh daily feed for a user.
 */
data class DailyFeedGeneratedEvent(
    override val eventId: String,
    override val occurredAt: Long,
    override val aggregateId: String,
    override val aggregateType: String = "DailyFeed",
    val itemCount: Int,
    val topFocusCardId: String?
) : DomainEvent {
    init {
        require(eventId.isNotBlank()) { "eventId cannot be blank" }
        require(occurredAt > 0) { "occurredAt timestamp must be positive" }
        require(aggregateId.isNotBlank()) { "aggregateId cannot be blank" }
        require(itemCount >= 0) { "itemCount cannot be negative" }
    }
}
