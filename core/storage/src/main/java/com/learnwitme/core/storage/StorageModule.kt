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
        .fallbackToDestructiveMigration()
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
}
