package com.learnwitme.core.storage.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Production Database Migration Infrastructure for LearnWitMeDatabase.
 * Defines explicit migrations between schema versions to prevent destructive migrations in production.
 */
object DatabaseMigrations {

    /**
     * Migration template from Schema Version 1 to Version 2.
     * Add explicit DDL statements here when schema updates are introduced in future releases.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Future DDL migration statements go here.
            // Example:
            // db.execSQL("ALTER TABLE user_profiles ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Complete list of registered Room migrations to pass into databaseBuilder.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2
    )
}
