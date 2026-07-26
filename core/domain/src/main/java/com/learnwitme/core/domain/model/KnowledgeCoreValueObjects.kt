package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Responsibility: Identifies a fine-grained technical capability (e.g. `sk_kotlin_coroutines`).
 */
@Serializable
data class Skill(
    val code: String,
    val name: String,
    val category: String = "GENERAL"
) : ValueObject {
    init {
        require(code.isNotBlank()) { "Skill code cannot be blank" }
        require(code.startsWith("sk_")) { "Skill code must start with 'sk_'" }
        require(name.isNotBlank()) { "Skill name cannot be blank" }
    }
}

enum class CompetencyLevel {
    NOVICE,
    COMPETENT,
    PROFICIENT,
    EXPERT
}

/**
 * Responsibility: Evaluates measured skill mastery level and numerical proficiency.
 */
@Serializable
data class Competency(
    val level: CompetencyLevel,
    val score: Double
) : ValueObject {
    init {
        require(score in 0.0..1.0) { "Competency score must be bounded between 0.0 and 1.0" }
    }
}

/**
 * Responsibility: Encapsulates structured learning material within a KnowledgeCard.
 */
@Serializable
data class KnowledgeContent(
    val title: String,
    val summary: String,
    val whyItMatters: String,
    val realWorldExample: String,
    val commonMistakes: String,
    val whenNotToUse: String = "",
    val bestPractice: String,
    val officialDocumentationLink: String = ""
) : ValueObject {
    init {
        require(title.isNotBlank()) { "KnowledgeContent title cannot be blank" }
        require(summary.isNotBlank()) { "KnowledgeContent summary cannot be blank" }
    }
}

/**
 * Responsibility: Bounded retention metric representing probability of recall (0.0 to 1.0).
 */
@Serializable
data class RetentionScore(val value: Double) : ValueObject {
    init {
        require(value in 0.0..1.0) { "Retention score must be bounded between 0.0 and 1.0" }
    }
}

/**
 * Responsibility: Memory stability duration metric measured in days.
 */
@Serializable
data class StabilityScore(val days: Double) : ValueObject {
    init {
        require(days >= 0.0) { "Stability score in days must be non-negative" }
    }
}

/**
 * Responsibility: Recommendation priority metric for feed and study queue ordering.
 */
@Serializable
data class PriorityScore(val value: Double) : ValueObject {
    init {
        require(value >= 0.0) { "Priority score must be non-negative" }
    }
}
