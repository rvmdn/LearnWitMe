package com.learnwitme.core.storage.source

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.storage.local.LearnWitMeDatabase
import com.learnwitme.core.storage.mapper.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LearnWitMeLocalDataSource(
    private val db: LearnWitMeDatabase
) {
    suspend fun getUserProfile(userId: UserId): UserProfile? = withContext(Dispatchers.IO) {
        db.userProfileDao().getUserProfile(userId.value)?.toDomain()
    }
    
    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        db.userProfileDao().insertUserProfile(profile.toEntity())
    }
    
    suspend fun getLearningMemory(userId: UserId): LearningMemory? = withContext(Dispatchers.IO) {
        val memoryEntity = db.learningMemoryDao().getMemoryByUserId(userId.value) ?: return@withContext null
        val recordsEntity = db.learningMemoryDao().getRecordsForMemory(memoryEntity.id)
        memoryEntity.toDomain(recordsEntity)
    }
    
    suspend fun saveLearningMemory(memory: LearningMemory) = withContext(Dispatchers.IO) {
        val memoryEntity = memory.toEntity()
        val recordsEntity = memory.records.map { it.toEntity(memory.id.value) }
        db.learningMemoryDao().insertMemory(memoryEntity)
        db.learningMemoryDao().insertRecords(recordsEntity)
        db.learningMemoryDao().deleteStaleRecords(memoryEntity.id, recordsEntity.map { it.cardId })
    }
    
    suspend fun getActiveRoadmap(userId: UserId): LearningRoadmap? = withContext(Dispatchers.IO) {
        val roadmapEntity = db.roadmapDao().getActiveRoadmap(userId.value) ?: return@withContext null
        val unitsEntity = db.roadmapDao().getUnitsForRoadmap(roadmapEntity.id)
        roadmapEntity.toDomain(unitsEntity)
    }
    
    suspend fun saveRoadmap(userId: UserId, roadmap: LearningRoadmap) = withContext(Dispatchers.IO) {
        val roadmapEntity = roadmap.toEntity(userId.value)
        val unitsEntity = roadmap.units.map { it.toEntity(roadmap.id.value) }
        db.roadmapDao().insertRoadmap(roadmapEntity)
        db.roadmapDao().insertUnits(unitsEntity)
    }
    
    suspend fun getSessions(userId: UserId): List<LearningSession> = withContext(Dispatchers.IO) {
        db.sessionDao().getSessionsByUserId(userId.value).map { it.toDomain() }
    }
    
    suspend fun saveSession(userId: UserId, session: LearningSession) = withContext(Dispatchers.IO) {
        db.sessionDao().insertSession(session.toEntity(userId.value))
    }
    
    suspend fun getKnowledgePackage(packageId: KnowledgePackageId): KnowledgePackage? = withContext(Dispatchers.IO) {
        val pkgEntity = db.knowledgePackageDao().getPackageById(packageId.value) ?: return@withContext null
        val cardsEntity = db.knowledgePackageDao().getCardsForPackage(pkgEntity.id)
        pkgEntity.toDomain(cardsEntity)
    }
    
    suspend fun saveKnowledgePackage(pkg: KnowledgePackage) = withContext(Dispatchers.IO) {
        val pkgEntity = pkg.toEntity()
        val cardsEntity = pkg.cards.map { it.toEntity(pkg.id.value) }
        db.knowledgePackageDao().insertPackage(pkgEntity)
        db.knowledgePackageDao().insertCards(cardsEntity)
    }

    suspend fun getActivePackages(): List<KnowledgePackage> = withContext(Dispatchers.IO) {
        db.knowledgePackageDao().getAllPackages().map { pkgEntity ->
            val cardsEntity = db.knowledgePackageDao().getCardsForPackage(pkgEntity.id)
            pkgEntity.toDomain(cardsEntity)
        }
    }
    
    suspend fun getKnowledgeCard(id: KnowledgeCardId): KnowledgeCard? = withContext(Dispatchers.IO) {
        db.knowledgePackageDao().getCardById(id.value)?.toDomain()
    }
    
    suspend fun saveKnowledgeCard(card: KnowledgeCard) = withContext(Dispatchers.IO) {
        // Without knowing packageId, we shouldn't insert a standalone card.
        // Wait, Domain KnowledgeCard does not have packageId!
        // But Entity has it. So saveKnowledgeCard is tricky if we don't know the packageId.
        // For simplicity, we can fetch the existing card to preserve packageId, or use a default "custom".
        val existing = db.knowledgePackageDao().getCardById(card.id.value)
        val packageId = existing?.packageId ?: "custom_package"
        db.knowledgePackageDao().insertCard(card.toEntity(packageId))
    }
    
    suspend fun deleteKnowledgeCard(id: KnowledgeCardId) = withContext(Dispatchers.IO) {
        db.knowledgePackageDao().deleteCard(id.value)
    }
    
    suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> = withContext(Dispatchers.IO) {
        db.knowledgePackageDao().getCardsByTopic("\"$topicCode\"").map { it.toDomain() }
    }
}
