package com.learnwitme.core.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.learnwitme.core.storage.local.dao.*
import com.learnwitme.core.storage.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        LearningMemoryEntity::class,
        MemoryRecordEntity::class,
        LearningRoadmapEntity::class,
        LearningUnitEntity::class,
        LearningSessionEntity::class,
        KnowledgePackageEntity::class,
        KnowledgeCardEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizOptionEntity::class,
        QuizResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LearnWitMeDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun learningMemoryDao(): LearningMemoryDao
    abstract fun roadmapDao(): RoadmapDao
    abstract fun sessionDao(): SessionDao
    abstract fun knowledgePackageDao(): KnowledgePackageDao
    abstract fun quizDao(): QuizDao
}
