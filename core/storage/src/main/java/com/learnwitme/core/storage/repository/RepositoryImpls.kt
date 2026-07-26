package com.learnwitme.core.storage.repository

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.storage.source.LearnWitMeLocalDataSource
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : UserProfileRepository {
    override suspend fun getUserProfile(userId: UserId): UserProfile? = localDataSource.getUserProfile(userId)
    override suspend fun saveUserProfile(profile: UserProfile) = localDataSource.saveUserProfile(profile)
}

class LearningMemoryRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : LearningMemoryRepository {
    override suspend fun getMemory(id: LearningMemoryId): LearningMemory? = localDataSource.getLearningMemoryById(id)
    override suspend fun getMemoryByUserId(userId: UserId): LearningMemory? = localDataSource.getLearningMemory(userId)
    override suspend fun saveMemory(memory: LearningMemory) = localDataSource.saveLearningMemory(memory)
}

class RoadmapRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : RoadmapRepository {
    override suspend fun getRoadmap(id: LearningRoadmapId): LearningRoadmap? = localDataSource.getRoadmapById(id)
    override suspend fun getActiveRoadmapByUserId(userId: UserId): LearningRoadmap? = localDataSource.getActiveRoadmap(userId)
    override suspend fun saveRoadmap(userId: UserId, roadmap: LearningRoadmap) = localDataSource.saveRoadmap(userId, roadmap)
}

class LearningSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : LearningSessionRepository {
    override suspend fun getSessions(userId: UserId): List<LearningSession> = localDataSource.getSessions(userId)
    override suspend fun saveSession(userId: UserId, session: LearningSession) = localDataSource.saveSession(userId, session)
}

class ProgressRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : ProgressRepository {
    override suspend fun getProgress(userId: UserId): LearningProgress? = localDataSource.getUserProfile(userId)?.progress
    override suspend fun saveProgress(userId: UserId, progress: LearningProgress) {
        val profile = localDataSource.getUserProfile(userId)
        if (profile != null) {
            val updated = profile.updateProgress(progress)
            localDataSource.saveUserProfile(updated)
        }
    }
}

class KnowledgePackageRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : KnowledgePackageRepository {
    override suspend fun getPackage(id: KnowledgePackageId): KnowledgePackage? = localDataSource.getKnowledgePackage(id)
    override suspend fun savePackage(pkg: KnowledgePackage) = localDataSource.saveKnowledgePackage(pkg)
    override suspend fun getActivePackages(): List<KnowledgePackage> = localDataSource.getActivePackages()
}

class KnowledgeRepositoryImpl @Inject constructor(
    private val localDataSource: LearnWitMeLocalDataSource
) : KnowledgeRepository {
    override suspend fun getCard(id: KnowledgeCardId): KnowledgeCard? = localDataSource.getKnowledgeCard(id)
    override suspend fun saveCard(card: KnowledgeCard) = localDataSource.saveKnowledgeCard(card)
    override suspend fun deleteCard(id: KnowledgeCardId) = localDataSource.deleteKnowledgeCard(id)
    override suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> = localDataSource.getCardsByTopic(topicCode)
}
