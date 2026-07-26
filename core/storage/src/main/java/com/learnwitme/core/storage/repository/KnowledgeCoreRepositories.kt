package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Recommendation
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.repository.LearningRepository
import com.learnwitme.core.domain.repository.RecommendationRepository
import com.learnwitme.core.storage.local.dao.KnowledgeCardDao
import com.learnwitme.core.storage.local.dao.KnowledgePackageDao
import com.learnwitme.core.storage.local.dao.RecommendationDao
import com.learnwitme.core.storage.local.dao.ReviewScheduleDao
import com.learnwitme.core.storage.local.dao.UserKnowledgeDao
import com.learnwitme.core.storage.mapper.KnowledgeMapper
import com.learnwitme.core.storage.mapper.LearningMapper
import com.learnwitme.core.storage.mapper.RecommendationMapper
import javax.inject.Inject

class KnowledgeRepositoryImpl @Inject constructor(
    private val packageDao: KnowledgePackageDao,
    private val cardDao: KnowledgeCardDao
) : KnowledgeRepository {

    override suspend fun findCard(id: KnowledgeCardId): KnowledgeCard? {
        val entity = cardDao.findById(id.value) ?: return null
        return KnowledgeMapper.toDomain(entity)
    }

    override suspend fun saveCard(card: KnowledgeCard) {
        val entity = KnowledgeMapper.toEntity(card)
        cardDao.insert(entity)
    }

    override suspend fun deleteCard(id: KnowledgeCardId) {
        cardDao.deleteCard(id.value)
    }

    override suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> {
        val entities = cardDao.getCardsByTopic("\"$topicCode\"")
        return entities.map { KnowledgeMapper.toDomain(it) }
    }

    override suspend fun searchBySkills(skills: List<Skill>): List<KnowledgeCard> {
        val allCards = cardDao.getAll()
        val targetSkillCodes = skills.map { it.code }.toSet()
        return allCards
            .map { KnowledgeMapper.toDomain(it) }
            .filter { card -> card.skills.any { targetSkillCodes.contains(it.code) } }
    }

    override suspend fun findPackage(packageId: KnowledgePackageId): KnowledgePackage? {
        val pkgEntity = packageDao.findById(packageId.value) ?: return null
        val cardEntities = cardDao.findByPackage(pkgEntity.id)
        return KnowledgeMapper.toDomain(pkgEntity, cardEntities)
    }

    override suspend fun importPackage(pkg: KnowledgePackage) {
        val pkgEntity = KnowledgeMapper.toEntity(pkg)
        val cardEntities = pkg.cards.map { KnowledgeMapper.toEntity(it, pkg.id.value) }
        packageDao.insert(pkgEntity)
        cardDao.insertAll(cardEntities)
    }

    override suspend fun publishPackage(packageId: KnowledgePackageId) {
        val pkgEntity = packageDao.findById(packageId.value) ?: return
        val updated = pkgEntity.copy(status = "PUBLISHED")
        packageDao.update(updated)
    }
}

class LearningRepositoryImpl @Inject constructor(
    private val userKnowledgeDao: UserKnowledgeDao,
    private val reviewScheduleDao: ReviewScheduleDao
) : LearningRepository {

    override suspend fun findUserKnowledge(userId: UserId): UserKnowledge? {
        val entity = userKnowledgeDao.findByUserId(userId.value) ?: return null
        return LearningMapper.toDomain(entity)
    }

    override suspend fun saveUserKnowledge(userKnowledge: UserKnowledge) {
        val entity = LearningMapper.toEntity(userKnowledge)
        userKnowledgeDao.save(entity)
    }

    override suspend fun startLearning(userId: UserId, cardId: KnowledgeCardId) {
        val existing = findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val updated = existing.startLearning(cardId)
        saveUserKnowledge(updated)
    }

    override suspend fun markCardMastered(userId: UserId, cardId: KnowledgeCardId) {
        val existing = findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val updated = existing.markCardMastered(cardId)
        saveUserKnowledge(updated)
    }

    override suspend fun updateCompetency(userId: UserId, skill: Skill, competency: Competency) {
        val existing = findUserKnowledge(userId) ?: UserKnowledge(id = userId)
        val updated = existing.updateCompetency(skill, competency)
        saveUserKnowledge(updated)
    }

    override suspend fun findReviewSchedule(userId: UserId, cardId: KnowledgeCardId): ReviewSchedule? {
        val entity = reviewScheduleDao.findByCard(userId.value, cardId.value) ?: return null
        return LearningMapper.toDomain(entity)
    }

    override suspend fun findDueReviews(userId: UserId, currentTimeMs: Long): List<ReviewSchedule> {
        val entities = reviewScheduleDao.findDueReviews(userId.value, currentTimeMs)
        return entities.map { LearningMapper.toDomain(it) }
    }

    override suspend fun recordReview(userId: UserId, cardId: KnowledgeCardId, schedule: ReviewSchedule) {
        val entity = LearningMapper.toEntity(userId, schedule)
        reviewScheduleDao.save(entity)
    }
}

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDao: RecommendationDao
) : RecommendationRepository {

    override suspend fun findRecommendation(userId: UserId): Recommendation? {
        val entities = recommendationDao.findByUser(userId.value)
        val topEntity = entities.firstOrNull() ?: return null
        return RecommendationMapper.toDomain(topEntity)
    }

    override suspend fun saveRecommendation(userId: UserId, recommendation: Recommendation) {
        val entity = RecommendationMapper.toEntity(userId, recommendation)
        recommendationDao.save(entity)
    }
}
