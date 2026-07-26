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
    @Query("SELECT * FROM learning_memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: String): LearningMemoryEntity?

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

// KnowledgePackageDao moved to KnowledgeCoreDaos.kt

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: String): QuizEntity?

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: String): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_options WHERE quizId = :quizId")
    suspend fun getOptionsForQuiz(quizId: String): List<QuizOptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<QuizOptionEntity>)

    @Query("SELECT * FROM quiz_results WHERE id = :id LIMIT 1")
    suspend fun getQuizResultById(id: String): QuizResultEntity?

    @Query("SELECT * FROM quiz_results WHERE userId = :userId")
    suspend fun getQuizResultsByUserId(userId: String): List<QuizResultEntity>

    @Query("SELECT * FROM quiz_results")
    suspend fun getAllQuizResults(): List<QuizResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)
}
