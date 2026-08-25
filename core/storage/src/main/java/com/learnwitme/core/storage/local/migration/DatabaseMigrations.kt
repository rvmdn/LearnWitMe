package com.learnwitme.core.storage.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Production Database Migration Infrastructure for LearnWitMeDatabase.
 * Defines explicit migrations between schema versions to prevent destructive migrations in production.
 */
object DatabaseMigrations {

    /** Adds immutable AI learning interaction artifacts. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_interactions` (`interactionId` TEXT NOT NULL, `userId` TEXT NOT NULL, `action` TEXT NOT NULL, `cardId` TEXT, `topicCode` TEXT, `topicName` TEXT, `topicDescription` TEXT, `startedAt` INTEGER NOT NULL, `completedAt` INTEGER, `provider` TEXT, `model` TEXT, `promptTokens` INTEGER NOT NULL, `completionTokens` INTEGER NOT NULL, `personalizationPreset` TEXT, PRIMARY KEY(`interactionId`))")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_learning_interactions_userId` ON `ai_learning_interactions` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_learning_interactions_startedAt` ON `ai_learning_interactions` (`startedAt`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_feedback` (`interactionId` TEXT NOT NULL, `usefulnessRating` INTEGER NOT NULL, `confidenceSignal` REAL, `comment` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`interactionId`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_outcomes` (`interactionId` TEXT NOT NULL, `status` TEXT NOT NULL, `summary` TEXT, `completedAt` INTEGER NOT NULL, `metadataJson` TEXT NOT NULL, PRIMARY KEY(`interactionId`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_memory_impacts` (`interactionId` TEXT NOT NULL, `cardId` TEXT, `topicCode` TEXT, `topicName` TEXT, `topicDescription` TEXT, `action` TEXT NOT NULL, `outcomeStatus` TEXT, `usefulnessRating` INTEGER NOT NULL, `confidenceSignal` REAL, `classification` TEXT NOT NULL, `confidenceDelta` REAL NOT NULL, `calculatedAt` INTEGER NOT NULL, PRIMARY KEY(`interactionId`))")
        }
    }

    /** Adds persistent analytics event queue for offline reliability. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_analytics_events` (`eventId` TEXT NOT NULL, `eventType` TEXT NOT NULL, `interactionId` TEXT NOT NULL, `userId` TEXT NOT NULL, `action` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `cardId` TEXT, `topicCode` TEXT, `provider` TEXT, `model` TEXT, `promptTokens` INTEGER NOT NULL, `completionTokens` INTEGER NOT NULL, `totalTokens` INTEGER NOT NULL, `personalizationPreset` TEXT, `outcomeStatus` TEXT, `summary` TEXT, `usefulnessRating` INTEGER, `confidenceSignal` REAL, `hasComment` INTEGER NOT NULL, `classification` TEXT, `confidenceDelta` REAL, `deliveryStatus` TEXT NOT NULL, `attemptCount` INTEGER NOT NULL, `lastAttemptAt` INTEGER, `nextRetryAt` INTEGER, `errorMessage` TEXT, PRIMARY KEY(`eventId`))")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_learning_analytics_events_interactionId` ON `ai_learning_analytics_events` (`interactionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_learning_analytics_events_deliveryStatus` ON `ai_learning_analytics_events` (`deliveryStatus`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_learning_analytics_events_userId` ON `ai_learning_analytics_events` (`userId`)")
        }
    }

    /**
     * Complete list of registered Room migrations to pass into databaseBuilder.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
