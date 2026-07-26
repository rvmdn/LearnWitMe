package com.learnwitme.core.storage

import android.content.Context
import androidx.room.Room
import com.learnwitme.core.storage.local.LearnWitMeDatabase
import com.learnwitme.core.storage.source.LearnWitMeLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

import com.learnwitme.core.security.DatabasePassphraseProvider

import com.learnwitme.core.storage.local.migration.DatabaseMigrations

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideLearnWitMeDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DatabasePassphraseProvider
    ): LearnWitMeDatabase {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
        val factory = SupportFactory(passphraseProvider.getPassphrase())

        return Room.databaseBuilder(
            context,
            LearnWitMeDatabase::class.java,
            "learnwitme_secure.db"
        )
        .openHelperFactory(factory)
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .build()
    }

    @Provides
    @Singleton
    fun provideLocalDataSource(db: LearnWitMeDatabase): LearnWitMeLocalDataSource {
        return LearnWitMeLocalDataSource(db)
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(db: LearnWitMeDatabase) = db.userProfileDao()

    @Provides
    @Singleton
    fun provideQuizDao(db: LearnWitMeDatabase) = db.quizDao()

    @Provides
    @Singleton
    fun provideKnowledgePackageDao(db: LearnWitMeDatabase) = db.knowledgePackageDao()

    @Provides
    @Singleton
    fun provideKnowledgeCardDao(db: LearnWitMeDatabase) = db.knowledgeCardDao()

    @Provides
    @Singleton
    fun provideUserKnowledgeDao(db: LearnWitMeDatabase) = db.userKnowledgeDao()

    @Provides
    @Singleton
    fun provideReviewScheduleDao(db: LearnWitMeDatabase) = db.reviewScheduleDao()

    @Provides
    @Singleton
    fun provideRecommendationDao(db: LearnWitMeDatabase) = db.recommendationDao()
}
