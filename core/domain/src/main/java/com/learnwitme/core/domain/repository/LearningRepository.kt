package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge

/**
 * Responsibility: Bounded Context repository contract managing dynamic learner state
 * (UserKnowledge, skill competencies, and spaced repetition ReviewSchedules).
 */
interface LearningRepository {
    // --- Learner State Management ---
    suspend fun findUserKnowledge(userId: UserId): UserKnowledge?
    suspend fun saveUserKnowledge(userKnowledge: UserKnowledge)
    suspend fun startLearning(userId: UserId, cardId: KnowledgeCardId)
    suspend fun markCardMastered(userId: UserId, cardId: KnowledgeCardId)
    suspend fun updateCompetency(userId: UserId, skill: Skill, competency: Competency)

    // --- Spaced Repetition Scheduling ---
    suspend fun findReviewSchedule(userId: UserId, cardId: KnowledgeCardId): ReviewSchedule?
    suspend fun findDueReviews(userId: UserId, currentTimeMs: Long): List<ReviewSchedule>
    suspend fun recordReview(userId: UserId, cardId: KnowledgeCardId, schedule: ReviewSchedule)
}
