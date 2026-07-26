package com.learnwitme.core.storage.di

import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.storage.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindLearningMemoryRepository(impl: LearningMemoryRepositoryImpl): LearningMemoryRepository

    @Binds
    @Singleton
    abstract fun bindRoadmapRepository(impl: RoadmapRepositoryImpl): RoadmapRepository

    @Binds
    @Singleton
    abstract fun bindLearningSessionRepository(impl: LearningSessionRepositoryImpl): LearningSessionRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindKnowledgePackageRepository(impl: KnowledgePackageRepositoryImpl): KnowledgePackageRepository

    @Binds
    @Singleton
    abstract fun bindKnowledgeRepository(impl: KnowledgeRepositoryImpl): KnowledgeRepository

    @Binds
    @Singleton
    abstract fun bindLearningRepository(impl: LearningRepositoryImpl): LearningRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: RecommendationRepositoryImpl): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository
}
