package com.learnwitme.app.di

import com.learnwitme.app.ui.navigation.*
import com.learnwitme.core.navigation.ScreenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppNavigationModule {

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindHomeScreenProvider(impl: HomeScreenProvider): ScreenProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindProfileScreenProvider(impl: ProfileScreenProvider): ScreenProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindKnowledgeCardScreenProvider(impl: KnowledgeCardScreenProvider): ScreenProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindQuizScreenProvider(impl: QuizScreenProvider): ScreenProvider

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindOnboardingScreenProvider(impl: OnboardingScreenProvider): ScreenProvider
}
