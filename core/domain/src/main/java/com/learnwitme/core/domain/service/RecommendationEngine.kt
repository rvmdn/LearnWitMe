package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*

/**
 * Responsibility: Stateless engine that evaluates and recommends the next best LearningUnit.
 */
class RecommendationEngine {

    fun generateRecommendation(
        roadmap: LearningRoadmap,
        memory: LearningMemory,
        reviewQueue: ReviewQueue,
        goal: LearningGoal,
        profile: UserProfile,
        knowledgePackage: KnowledgePackage,
        policy: RecommendationPolicy
    ): RecommendationDecision {

        val candidates = mutableListOf<RecommendationCandidate>()
        val weakTopics = memory.detectWeakTopics { cardId ->
            roadmap.units.find { it.cardId == cardId }?.topicCode ?: ""
        }
        val nextPendingUnit = roadmap.getNextPendingUnit()

        for (unit in roadmap.units) {
            val record = memory.findRecord(unit.cardId)
            val reviewDecision = reviewQueue.decisions.find { it.cardId == unit.cardId }
            val card = knowledgePackage.cards.find { it.id == unit.cardId }

            if (card == null) continue

            // 1. Filtering Phase
            // Rule: Do not recommend MASTERED cards unless they are in the review queue
            if (record != null && record.state == MemoryState.MASTERED && reviewDecision == null) {
                continue
            }

            // Rule: Avoid repeating materials that are already being learned/remembered but not yet due for review
            // (If it's already in memory but not NEW and not MASTERED, and not in the review queue)
            if (record != null && record.state != MemoryState.NEW && record.state != MemoryState.MASTERED && reviewDecision == null) {
                continue
            }

            var score = 0.0
            val reasons = mutableListOf<String>()

            // 2. Scoring Phase
            // Rule: Prioritize ReviewQueue over new materials
            if (reviewDecision != null) {
                when (reviewDecision.priority) {
                    ReviewPriority.CRITICAL -> {
                        score += 50.0
                        reasons.add("Critical review priority")
                    }
                    ReviewPriority.HIGH -> {
                        score += 40.0
                        reasons.add("High review priority")
                    }
                    ReviewPriority.MEDIUM -> {
                        score += 30.0
                        reasons.add("Medium review priority")
                    }
                    ReviewPriority.LOW -> {
                        score += 20.0
                        reasons.add("Low review priority")
                    }
                }
            } else if (record == null || record.state == MemoryState.NEW) {
                score += 5.0
                reasons.add("New material")
            }

            // Rule: Roadmap Position (Next Pending Unit)
            if (nextPendingUnit != null && unit.id == nextPendingUnit.id) {
                score += 35.0
                reasons.add("Next pending unit in your roadmap")
            }

            // Rule: Weak Topic Priority
            if (weakTopics.contains(unit.topicCode)) {
                score += 25.0
                reasons.add("Addresses a weak topic")
            }

            // Rule: Difficulty Match
            if (card.difficulty == profile.currentLevel || card.difficulty == profile.targetLevel) {
                score += 10.0
                reasons.add("Matches your difficulty level")
            }

            // Rule: Learning Goal Match
            if (roadmap.goal == goal) {
                score += 5.0
                reasons.add("Aligns with your learning goal")
            }

            if (score > 0.0) {
                candidates.add(RecommendationCandidate(unit, score, reasons))
            }
        }

        // Sort descending by score
        candidates.sortByDescending { it.score }

        val topRecommendation = candidates.firstOrNull()
        val alternatives = if (candidates.size > 1) {
            candidates.drop(1).take(policy.maxAlternatives)
        } else {
            emptyList()
        }

        return RecommendationDecision(
            topRecommendation = topRecommendation,
            alternatives = alternatives
        )
    }

    /**
     * Pure, deterministic recommendation algorithm evaluating UserKnowledge, due ReviewSchedules,
     * and candidate KnowledgeCards to produce a prioritized recommendation list.
     */
    fun generateKnowledgeRecommendation(
        userId: UserId,
        userKnowledge: UserKnowledge,
        dueSchedules: List<ReviewSchedule>,
        availableCards: List<KnowledgeCard>,
        currentTimeMs: Long
    ): List<Recommendation> {
        require(currentTimeMs > 0) { "Current time timestamp must be positive" }

        val recommendations = mutableListOf<Recommendation>()
        val dueCardIds = dueSchedules.map { it.cardId }.toSet()

        for (card in availableCards) {
            // 1. Skip already mastered cards unless explicitly due for review
            if (userKnowledge.masteredCardIds.contains(card.id) && !dueCardIds.contains(card.id)) {
                continue
            }

            var score = 0.0
            val reasons = mutableListOf<String>()

            // 2. High priority score for due reviews
            if (dueCardIds.contains(card.id)) {
                score += 50.0
                reasons.add("Card is due for spaced-repetition review")
            }

            // 3. Active learning cards priority
            if (userKnowledge.activeCardIds.contains(card.id)) {
                score += 30.0
                reasons.add("Card is in your active learning list")
            } else if (!userKnowledge.masteredCardIds.contains(card.id)) {
                score += 10.0
                reasons.add("New card matching your curriculum")
            }

            // 4. Prerequisite readiness check
            val unmetPrerequisites = card.prerequisiteCardIds.filter { !userKnowledge.masteredCardIds.contains(it) }
            if (unmetPrerequisites.isNotEmpty()) {
                score -= 25.0 // Penalize if prerequisites are not yet completed
            }

            if (score > 0.0) {
                val reasonString = reasons.joinToString("; ")
                recommendations.add(Recommendation(card.id, (score / 100.0).coerceIn(0.0, 1.0), reasonString))
            }
        }

        return recommendations.sortedByDescending { it.score }
    }
}
