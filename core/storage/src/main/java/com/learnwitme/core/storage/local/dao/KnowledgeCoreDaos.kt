package com.learnwitme.core.storage.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.learnwitme.core.storage.local.entity.KnowledgeCardEntity
import com.learnwitme.core.storage.local.entity.KnowledgePackageEntity
import com.learnwitme.core.storage.local.entity.RecommendationEntity
import com.learnwitme.core.storage.local.entity.ReviewScheduleEntity
import com.learnwitme.core.storage.local.entity.UserKnowledgeEntity

@Dao
interface KnowledgePackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(packageEntity: KnowledgePackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: KnowledgePackageEntity) = insert(pkg)

    @Update
    suspend fun update(packageEntity: KnowledgePackageEntity)

    @Delete
    suspend fun delete(packageEntity: KnowledgePackageEntity)

    @Query("SELECT * FROM knowledge_packages WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): KnowledgePackageEntity?

    @Query("SELECT * FROM knowledge_packages WHERE id = :packageId LIMIT 1")
    suspend fun getPackageById(packageId: String): KnowledgePackageEntity? = findById(packageId)

    @Query("SELECT * FROM knowledge_packages")
    suspend fun getAllPackages(): List<KnowledgePackageEntity>

    @Query("SELECT * FROM knowledge_packages WHERE status = 'PUBLISHED' OR isCore = 1")
    suspend fun getPublishedPackages(): List<KnowledgePackageEntity>
}

@Dao
interface KnowledgeCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<KnowledgeCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<KnowledgeCardEntity>) = insertAll(cards)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: KnowledgeCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: KnowledgeCardEntity) = insert(card)

    @Update
    suspend fun update(card: KnowledgeCardEntity)

    @Query("SELECT * FROM knowledge_cards WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): KnowledgeCardEntity?

    @Query("SELECT * FROM knowledge_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): KnowledgeCardEntity? = findById(id)

    @Query("SELECT * FROM knowledge_cards WHERE packageId = :packageId")
    suspend fun findByPackage(packageId: String): List<KnowledgeCardEntity>

    @Query("SELECT * FROM knowledge_cards WHERE packageId = :packageId")
    suspend fun getCardsForPackage(packageId: String): List<KnowledgeCardEntity> = findByPackage(packageId)

    @Query("SELECT * FROM knowledge_cards WHERE title LIKE '%' || :query || '%'")
    suspend fun searchByTitle(query: String): List<KnowledgeCardEntity>

    @Query("SELECT * FROM knowledge_cards WHERE relatedTopicCodesJson LIKE '%' || :topicCode || '%'")
    suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCardEntity>

    @Query("DELETE FROM knowledge_cards WHERE id = :id")
    suspend fun deleteCard(id: String)

    @Query("SELECT * FROM knowledge_cards")
    suspend fun getAll(): List<KnowledgeCardEntity>
}

@Dao
interface UserKnowledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(userKnowledge: UserKnowledgeEntity)

    @Update
    suspend fun update(userKnowledge: UserKnowledgeEntity)

    @Query("SELECT * FROM user_knowledge WHERE userId = :userId LIMIT 1")
    suspend fun findByUserId(userId: String): UserKnowledgeEntity?
}

@Dao
interface ReviewScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(schedule: ReviewScheduleEntity)

    @Update
    suspend fun update(schedule: ReviewScheduleEntity)

    @Query("SELECT * FROM review_schedules WHERE userId = :userId AND nextReviewTimestamp <= :currentTimeMs ORDER BY nextReviewTimestamp ASC")
    suspend fun findDueReviews(userId: String, currentTimeMs: Long): List<ReviewScheduleEntity>

    @Query("SELECT * FROM review_schedules WHERE userId = :userId AND cardId = :cardId LIMIT 1")
    suspend fun findByCard(userId: String, cardId: String): ReviewScheduleEntity?
}

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(recommendation: RecommendationEntity)

    @Query("SELECT * FROM recommendations WHERE userId = :userId ORDER BY score DESC")
    suspend fun findByUser(userId: String): List<RecommendationEntity>

    @Query("DELETE FROM recommendations WHERE userId = :userId")
    suspend fun deleteOldRecommendations(userId: String)
}
