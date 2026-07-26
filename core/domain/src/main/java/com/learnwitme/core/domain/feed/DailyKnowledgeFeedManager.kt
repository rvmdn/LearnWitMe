package com.learnwitme.core.domain.feed

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.LearningRepository

/**
 * Responsibility: Pure domain service engine responsible for generating personalized, deterministic
 * daily study feeds by prioritizing due reviews, targeting weak skills, balancing new topic exploration,
 * and maintaining configurable daily card limits.
 */
class DailyKnowledgeFeedManager(
    private val learningRepository: LearningRepository
) {
    suspend fun generateDailyFeed(
        userId: UserId,
        availableCards: List<KnowledgeCard>,
        config: DailyFeedConfiguration = DailyFeedConfiguration(),
        currentTimeMs: Long
    ): DailyFeedResult {
        val uniqueCards = availableCards.distinctBy { it.id.value }
        val userKnowledge = learningRepository.findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val dueSchedules = learningRepository.findDueReviews(userId, currentTimeMs)
        val dueCardIdSet = dueSchedules.map { it.cardId.value }.toSet()

        val activeCardIds = userKnowledge.activeCardIds.map { it.value }.toSet()
        val masteredCardIds = userKnowledge.masteredCardIds.map { it.value }.toSet()

        val dueReviewItems = mutableListOf<DailyFeedItem>()
        val weakSkillItems = mutableListOf<DailyFeedItem>()
        val newTopicItems = mutableListOf<DailyFeedItem>()
        val recommendedItems = mutableListOf<DailyFeedItem>()

        for (card in uniqueCards) {
            val cardIdStr = card.id.value
            val isDue = dueCardIdSet.contains(cardIdStr)
            val isStudying = activeCardIds.contains(cardIdStr)
            val isMastered = masteredCardIds.contains(cardIdStr)

            when {
                isDue -> {
                    val schedule = dueSchedules.firstOrNull { it.cardId.value == cardIdStr }
                    val priority = schedule?.easeFactor ?: 2.5
                    dueReviewItems.add(DailyFeedItem(card, FeedReason.ReviewDue, priority))
                }
                isStudying && !isMastered -> {
                    weakSkillItems.add(DailyFeedItem(card, FeedReason.WeakSkill, 2.0))
                }
                !isStudying && !isMastered -> {
                    newTopicItems.add(DailyFeedItem(card, FeedReason.NewTopic, 1.5))
                }
                else -> {
                    recommendedItems.add(DailyFeedItem(card, FeedReason.Recommended, 1.0))
                }
            }
        }

        // Deterministic sorting within priority groups using cardId
        dueReviewItems.sortByDescending { it.card.id.value }
        weakSkillItems.sortBy { it.card.id.value }
        newTopicItems.sortBy { it.card.id.value }
        recommendedItems.sortBy { it.card.id.value }

        val targetReviewQuota = (config.dailyCardLimit * config.reviewWeight).toInt()
        val targetNewQuota = config.dailyCardLimit - targetReviewQuota

        val selectedItems = mutableListOf<DailyFeedItem>()

        // 1. Fill review quota
        selectedItems.addAll(dueReviewItems.take(targetReviewQuota))
        if (selectedItems.size < targetReviewQuota) {
            val needed = targetReviewQuota - selectedItems.size
            selectedItems.addAll(weakSkillItems.take(needed))
        }

        // 2. Fill new quota
        val newSelected = mutableListOf<DailyFeedItem>()
        newSelected.addAll(newTopicItems.take(targetNewQuota))
        if (newSelected.size < targetNewQuota) {
            val needed = targetNewQuota - newSelected.size
            newSelected.addAll(recommendedItems.take(needed))
        }

        selectedItems.addAll(newSelected)

        // 3. Backfill if still below daily limit
        if (selectedItems.size < config.dailyCardLimit) {
            val remainingLimit = config.dailyCardLimit - selectedItems.size
            val alreadySelectedIds = selectedItems.map { it.card.id.value }.toSet()
            val candidatePool = (dueReviewItems + weakSkillItems + newTopicItems + recommendedItems)
                .filter { !alreadySelectedIds.contains(it.card.id.value) }
            selectedItems.addAll(candidatePool.take(remainingLimit))
        }

        val finalFeed = selectedItems.take(config.dailyCardLimit)
        val reviewCount = finalFeed.count { it.reason == FeedReason.ReviewDue || it.reason == FeedReason.WeakSkill }
        val newCardCount = finalFeed.size - reviewCount
        val totalEstSeconds = finalFeed.sumOf { it.card.estimatedReadingTimeSeconds.toLong() }

        return DailyFeedResult(
            feedItems = finalFeed,
            reviewCount = reviewCount,
            newCardCount = newCardCount,
            estimatedStudyTimeSeconds = totalEstSeconds
        )
    }
}
