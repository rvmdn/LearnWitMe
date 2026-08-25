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
        UserKnowledgeEntity::class,
        ReviewScheduleEntity::class,
        RecommendationEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizOptionEntity::class,
        QuizResultEntity::class,
        AILearningInteractionEntity::class,
        AILearningFeedbackEntity::class,
        AILearningOutcomeEntity::class,
        AILearningMemoryImpactEntity::class,
        AILearningAnalyticsEventEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class LearnWitMeDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun learningMemoryDao(): LearningMemoryDao
    abstract fun roadmapDao(): RoadmapDao
    abstract fun sessionDao(): SessionDao
    abstract fun knowledgePackageDao(): KnowledgePackageDao
    abstract fun knowledgeCardDao(): KnowledgeCardDao
    abstract fun userKnowledgeDao(): UserKnowledgeDao
    abstract fun reviewScheduleDao(): ReviewScheduleDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun quizDao(): QuizDao
    abstract fun aiLearningInteractionDao(): AILearningInteractionDao
    abstract fun aiLearningAnalyticsEventDao(): AILearningAnalyticsEventDao
}
