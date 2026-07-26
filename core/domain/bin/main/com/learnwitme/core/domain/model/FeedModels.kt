package com.learnwitme.core.domain.model

enum class FeedSectionType {
    TODAYS_FOCUS,
    CONTINUE_LEARNING,
    REVIEW_TODAY,
    EXPLORE,
    PROGRESS,
    ACHIEVEMENT
}

data class FeedCard(
    val cardId: KnowledgeCardId,
    val actionType: String
) : ValueObject

data class FeedSection(
    val type: FeedSectionType,
    val title: String,
    val items: List<FeedCard>,
    val summaryData: ProgressSummary? = null
) : ValueObject

data class DailyFeed(
    val sections: List<FeedSection>
) : ValueObject

data class FeedLayout(
    val template: String
) : ValueObject

data class FeedSummary(
    val totalActionableItems: Int
) : ValueObject

data class FeedPolicy(
    val maxReviewCards: Int = 5,
    val maxContinueCards: Int = 3,
    val maxExploreCards: Int = 3,
    val hideEmptySections: Boolean = true
) : ValueObject

data class FeedDecision(
    val feed: DailyFeed,
    val layout: FeedLayout,
    val summary: FeedSummary
) : ValueObject
