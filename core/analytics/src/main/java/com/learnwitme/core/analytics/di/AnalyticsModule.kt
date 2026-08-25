package com.learnwitme.core.analytics.di

import com.learnwitme.core.analytics.AILearningAnalyticsDispatcher
import com.learnwitme.core.analytics.AILearningAnalyticsEventMapper
import com.learnwitme.core.analytics.AILearningAnalyticsPipeline
import com.learnwitme.core.analytics.AILearningAnalyticsRepository
import com.learnwitme.core.analytics.PersistentAILearningAnalyticsDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module providing learning analytics pipeline and persistent dispatcher.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAILearningAnalyticsEventMapper(): AILearningAnalyticsEventMapper {
        return AILearningAnalyticsEventMapper()
    }

    @Provides
    @Singleton
    fun provideAILearningAnalyticsDispatcher(
        repository: AILearningAnalyticsRepository
    ): AILearningAnalyticsDispatcher {
        return PersistentAILearningAnalyticsDispatcher(repository = repository)
    }

    @Provides
    @Singleton
    fun provideAILearningAnalyticsPipeline(
        dispatcher: AILearningAnalyticsDispatcher,
        mapper: AILearningAnalyticsEventMapper
    ): AILearningAnalyticsPipeline {
        return AILearningAnalyticsPipeline(
            dispatcher = dispatcher,
            mapper = mapper
        )
    }
}
