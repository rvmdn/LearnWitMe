package com.learnwitme.core.storage.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.learnwitme.core.storage.local.entity.*

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)
}

@Dao
interface LearningMemoryDao {
    @Transaction
    @Query("SELECT * FROM learning_memories WHERE userId = :userId LIMIT 1")
    suspend fun getMemoryByUserId(userId: String): LearningMemoryEntity?

    @Query("SELECT * FROM memory_records WHERE memoryId = :memoryId")
    suspend fun getRecordsForMemory(memoryId: String): List<MemoryRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: LearningMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<MemoryRecordEntity>)
    
    @Query("DELETE FROM memory_records WHERE memoryId = :memoryId AND cardId NOT IN (:currentCardIds)")
    suspend fun deleteStaleRecords(memoryId: String, currentCardIds: List<String>)
}

@Dao
interface RoadmapDao {
    @Query("SELECT * FROM learning_roadmaps WHERE userId = :userId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveRoadmap(userId: String): LearningRoadmapEntity?
    
    @Query("SELECT * FROM learning_roadmaps WHERE id = :roadmapId LIMIT 1")
    suspend fun getRoadmapById(roadmapId: String): LearningRoadmapEntity?

    @Query("SELECT * FROM learning_units WHERE roadmapId = :roadmapId")
    suspend fun getUnitsForRoadmap(roadmapId: String): List<LearningUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: LearningRoadmapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<LearningUnitEntity>)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM learning_sessions WHERE userId = :userId")
    suspend fun getSessionsByUserId(userId: String): List<LearningSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LearningSessionEntity)
}

@Dao
interface KnowledgePackageDao {
    @Query("SELECT * FROM knowledge_packages WHERE id = :packageId LIMIT 1")
    suspend fun getPackageById(packageId: String): KnowledgePackageEntity?

    @Query("SELECT * FROM knowledge_packages")
    suspend fun getAllPackages(): List<KnowledgePackageEntity>

    @Query("SELECT * FROM knowledge_cards WHERE packageId = :packageId")
    suspend fun getCardsForPackage(packageId: String): List<KnowledgeCardEntity>

    @Query("SELECT * FROM knowledge_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): KnowledgeCardEntity?
    
    @Query("SELECT * FROM knowledge_cards WHERE relatedTopicCodesJson LIKE '%' || :topicCode || '%'")
    suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCardEntity>
    
    @Query("DELETE FROM knowledge_cards WHERE id = :id")
    suspend fun deleteCard(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: KnowledgePackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<KnowledgeCardEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: KnowledgeCardEntity)
}
