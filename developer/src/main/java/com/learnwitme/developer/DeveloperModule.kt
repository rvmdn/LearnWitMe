package com.learnwitme.developer

import com.learnwitme.core.navigation.ScreenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeveloperModule {

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindDeveloperConsoleProvider(
        provider: DeveloperConsoleProvider
    ): ScreenProvider
}
