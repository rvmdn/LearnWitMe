package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*

/**
 * Responsibility: Composer Layer that builds the Daily Learning Experience (Feed).
 * It does not calculate anything on its own, it only orders and filters outputs from other engines.
 */
class FeedEngine {

    fun composeFeed(
        recommendation: RecommendationDecision,
        progress: ProgressSummary,
        reviewQueue: ReviewQueue,
        knowledge: KnowledgeDecision,
        memory: LearningMemory,
        profile: UserProfile,
        policy: FeedPolicy
    ): FeedDecision {
        val usedCardIds = mutableSetOf<KnowledgeCardId>()
        val sections = mutableListOf<FeedSection>()
        
        // 1. Today's Focus (Hero Recommendation)
        val hero = recommendation.topRecommendation
        val focusItems = mutableListOf<FeedCard>()
        if (hero != null && !usedCardIds.contains(hero.unit.cardId)) {
            focusItems.add(FeedCard(hero.unit.cardId, "START"))
            usedCardIds.add(hero.unit.cardId)
        }
        if (focusItems.isNotEmpty() || !policy.hideEmptySections) {
            sections.add(FeedSection(FeedSectionType.TODAYS_FOCUS, "Today's Focus", focusItems))
        }
        
        // 2. Continue Learning
        val continueLearningIds = memory.records
            .filter { it.state == MemoryState.LEARNING }
            .map { it.cardId }
            .filter { !usedCardIds.contains(it) }
            .take(policy.maxContinueCards)
            
        val continueItems = continueLearningIds.map { FeedCard(it, "CONTINUE") }
        if (continueItems.isNotEmpty() || !policy.hideEmptySections) {
            usedCardIds.addAll(continueLearningIds)
            sections.add(FeedSection(FeedSectionType.CONTINUE_LEARNING, "Continue Learning", continueItems))
        }
        
        // 3. Review Today
        val reviewIds = reviewQueue.decisions
            .map { it.cardId }
            .filter { !usedCardIds.contains(it) }
            .take(policy.maxReviewCards)
            
        val reviewItems = reviewIds.map { FeedCard(it, "REVIEW") }
        if (reviewItems.isNotEmpty() || !policy.hideEmptySections) {
            usedCardIds.addAll(reviewIds)
            sections.add(FeedSection(FeedSectionType.REVIEW_TODAY, "Review Today", reviewItems))
        }
        
        // 4. Explore
        val exploreIds = recommendation.alternatives
            .map { it.unit.cardId }
            .filter { !usedCardIds.contains(it) }
            .take(policy.maxExploreCards)
            
        val exploreItems = exploreIds.map { FeedCard(it, "EXPLORE") }
        if (exploreItems.isNotEmpty() || !policy.hideEmptySections) {
            usedCardIds.addAll(exploreIds)
            sections.add(FeedSection(FeedSectionType.EXPLORE, "Explore", exploreItems))
        }
        
        // 5. Progress
        sections.add(FeedSection(FeedSectionType.PROGRESS, "Your Progress", emptyList(), progress))
        
        // 6. Achievement
        sections.add(FeedSection(FeedSectionType.ACHIEVEMENT, "Achievements", emptyList(), progress))
        
        // Count actionable items
        val totalActionable = focusItems.size + continueItems.size + reviewItems.size + exploreItems.size
        
        return FeedDecision(
            feed = DailyFeed(sections),
            layout = FeedLayout("STANDARD_DASHBOARD"),
            summary = FeedSummary(totalActionable)
        )
    }
}
