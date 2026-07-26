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

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideLearnWitMeDatabase(
        @ApplicationContext context: Context
    ): LearnWitMeDatabase {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
        val passphrase = "SecureLearnWitMePassphrase123!".toByteArray()
        val factory = SupportFactory(passphrase)

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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
        val passphrase = "SecureLearnWitMePassphrase123!".toByteArray()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "learnwitme_app_secure.db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}
