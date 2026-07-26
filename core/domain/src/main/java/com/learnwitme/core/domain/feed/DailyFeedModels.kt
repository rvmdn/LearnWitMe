package com.learnwitme.core.domain.feed

import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard

/**
 * Business rationale why a card was selected for the daily study feed.
 */
enum class FeedReason {
    ReviewDue,
    WeakSkill,
    NewTopic,
    Recommended
}

/**
 * Configurable parameters governing daily feed allocation and difficulty boundaries.
 */
data class DailyFeedConfiguration(
    val dailyCardLimit: Int = 10,
    val reviewWeight: Double = 0.7,
    val newCardWeight: Double = 0.3,
    val minimumDifficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val maximumDifficulty: DifficultyLevel = DifficultyLevel.PRINCIPAL
) {
    init {
        require(dailyCardLimit > 0) { "dailyCardLimit must be greater than zero" }
        require(reviewWeight in 0.0..1.0) { "reviewWeight must be between 0.0 and 1.0" }
        require(newCardWeight in 0.0..1.0) { "newCardWeight must be between 0.0 and 1.0" }
    }
}

/**
 * Individual item inside a daily knowledge feed.
 */
data class DailyFeedItem(
    val card: KnowledgeCard,
    val reason: FeedReason,
    val priorityScore: Double
)

/**
 * Metrics summary of a generated daily feed.
 */
data class DailyFeedResult(
    val feedItems: List<DailyFeedItem>,
    val reviewCount: Int,
    val newCardCount: Int,
    val estimatedStudyTimeSeconds: Long
)
