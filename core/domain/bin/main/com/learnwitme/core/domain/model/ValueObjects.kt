package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(val name: String) : ValueObject {
    init {
        require(name.isNotBlank()) { "Tag name cannot be blank" }
        require(name.trim() == name) { "Tag name cannot contain leading or trailing whitespace" }
    }
}

@Serializable
data class Reference(
    val title: String,
    val url: String? = null
) : ValueObject {
    init {
        require(title.isNotBlank()) { "Reference title cannot be blank" }
        if (url != null) {
            require(url.isNotBlank()) { "Reference URL cannot be blank if provided" }
            require(url.startsWith("http://") || url.startsWith("https://")) {
                "Reference URL must be a valid http or https URL"
            }
        }
    }
}

@Serializable
data class Topic(
    val code: String,
    val name: String,
    val description: String
) : ValueObject {
    init {
        require(code.isNotBlank()) { "Topic code cannot be blank" }
        require(code.startsWith("tp_")) { "Topic code must start with 'tp_'" }
        require(name.isNotBlank()) { "Topic name cannot be blank" }
    }
}

@Serializable
data class SubTopic(
    val code: String,
    val parentTopicCode: String,
    val name: String,
    val description: String
) : ValueObject {
    init {
        require(code.isNotBlank()) { "SubTopic code cannot be blank" }
        require(code.startsWith("sub_")) { "SubTopic code must start with 'sub_'" }
        require(parentTopicCode.isNotBlank()) { "Parent topic code cannot be blank" }
        require(name.isNotBlank()) { "SubTopic name cannot be blank" }
    }
}

@Serializable
data class CareerPath(
    val code: String,
    val name: String,
    val description: String
) : ValueObject {
    init {
        require(code.isNotBlank()) { "CareerPath code cannot be blank" }
        require(code.startsWith("cp_")) { "CareerPath code must start with 'cp_'" }
        require(name.isNotBlank()) { "CareerPath name cannot be blank" }
    }
}

@Serializable
data class KnowledgeScore(
    val topicCode: String,
    val score: Double,
    val lastUpdated: Long
) : ValueObject {
    init {
        require(topicCode.isNotBlank()) { "Topic code cannot be blank" }
        require(score in 0.0..1.0) { "Score must be between 0.0 and 1.0 (inclusive)" }
        require(lastUpdated > 0) { "Last updated timestamp must be positive" }
    }
}

@Serializable
data class ReviewSchedule(
    val cardId: KnowledgeCardId,
    val intervalDays: Int,
    val nextReviewTimestamp: Long,
    val easeFactor: Double,
    val repetitions: Int
) : ValueObject {
    init {
        require(intervalDays >= 0) { "Interval days cannot be negative" }
        require(nextReviewTimestamp > 0) { "Next review timestamp must be positive" }
        require(easeFactor >= 1.3) { "Ease factor must be at least 1.3" }
        require(repetitions >= 0) { "Repetitions count cannot be negative" }
    }

    /**
     * Records a review outcome and calculates the next spaced-repetition schedule.
     */
    fun recordReview(qualityScore: Double, clockTimestamp: Long): ReviewSchedule {
        require(qualityScore in 0.0..1.0) { "Quality score must be bounded between 0.0 and 1.0" }
        val newRepetitions = repetitions + 1
        val newEaseFactor = (easeFactor + (0.1 - (1.0 - qualityScore) * 0.2)).coerceAtLeast(1.3)
        val newInterval = if (newRepetitions == 1) 1 else (intervalDays * newEaseFactor).toInt().coerceAtLeast(1)
        val nextTimestamp = clockTimestamp + (newInterval * 24 * 3600 * 1000L)
        return this.copy(
            intervalDays = newInterval,
            nextReviewTimestamp = nextTimestamp,
            easeFactor = newEaseFactor,
            repetitions = newRepetitions
        )
    }

    /**
     * Postpones the scheduled review date by a specified number of days.
     */
    fun postponeReview(postponeDays: Int): ReviewSchedule {
        require(postponeDays > 0) { "Postpone days must be greater than zero" }
        val extensionMs = postponeDays * 24 * 3600 * 1000L
        return this.copy(
            intervalDays = intervalDays + postponeDays,
            nextReviewTimestamp = nextReviewTimestamp + extensionMs
        )
    }

    /**
     * Resets the schedule back to initial learning state.
     */
    fun resetSchedule(clockTimestamp: Long): ReviewSchedule {
        return this.copy(
            intervalDays = 1,
            nextReviewTimestamp = clockTimestamp + (24 * 3600 * 1000L),
            easeFactor = 2.5,
            repetitions = 0
        )
    }
}

@Serializable
data class LearningProgress(
    val totalUnitsCompleted: Int,
    val totalCardsMastered: Int,
    val activeStreakDays: Int,
    val lastActiveDate: Long
) : ValueObject {
    init {
        require(totalUnitsCompleted >= 0) { "Total units completed cannot be negative" }
        require(totalCardsMastered >= 0) { "Total cards mastered cannot be negative" }
        require(activeStreakDays >= 0) { "Active streak days cannot be negative" }
        require(lastActiveDate >= 0) { "Last active date timestamp must be non-negative" }
    }
}

@Serializable
data class LearningGoal(
    val description: String,
    val careerPathCode: String,
    val targetLevel: DifficultyLevel
) : ValueObject {
    init {
        require(description.isNotBlank()) { "Learning goal description cannot be blank" }
        require(careerPathCode.isNotBlank()) { "Career path code cannot be blank" }
    }
}

@Serializable
data class Recommendation(
    val cardId: KnowledgeCardId,
    val score: Double,
    val reason: String
) : ValueObject {
    init {
        require(score in 0.0..1.0) { "Recommendation score must be between 0.0 and 1.0" }
        require(reason.isNotBlank()) { "Reason for recommendation cannot be blank" }
    }
}

@Serializable
data class LearningPreference(
    val preferredTopics: List<String>,
    val dailyLimitMinutes: Int
) : ValueObject {
    init {
        require(preferredTopics.isNotEmpty()) { "Preferred topics cannot be empty" }
        require(dailyLimitMinutes > 0) { "Daily learning limit must be positive" }
    }
}

@Serializable
data class ReviewPolicy(
    val minIntervalSeconds: Long = 60L,
    val maxIntervalSeconds: Long = 30 * 24 * 3600L,
    val criticalRetentionThreshold: Double = 0.8
) : ValueObject {
    init {
        require(minIntervalSeconds > 0) { "Min interval must be positive" }
        require(maxIntervalSeconds >= minIntervalSeconds) { "Max interval must be greater than or equal to min interval" }
        require(criticalRetentionThreshold in 0.0..1.0) { "Critical retention threshold must be in 0.0..1.0" }
    }
}

enum class ReviewPriority {
    CRITICAL, HIGH, MEDIUM, LOW
}

data class ReviewDecision(
    val cardId: KnowledgeCardId,
    val nextReviewTime: Long,
    val priority: ReviewPriority,
    val isOverdue: Boolean,
    val canBeDeferred: Boolean,
    val reason: String
) : ValueObject {
    init {
        require(nextReviewTime > 0) { "Next review time must be positive" }
        require(reason.isNotBlank()) { "Review reason cannot be blank" }
    }
}

data class ReviewQueue(
    val decisions: List<ReviewDecision>
) : ValueObject

