package com.learnwitme.core.config

import android.content.Context
import com.learnwitme.core.storage.EncryptedDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideConfigManager(
        @ApplicationContext context: Context,
        encryptedDataStore: EncryptedDataStore
    ): ConfigManager {
        return ConfigManager(context, encryptedDataStore)
    }
}
