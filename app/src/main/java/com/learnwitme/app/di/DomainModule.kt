package com.learnwitme.app.di

import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import com.learnwitme.core.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class SystemClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = SystemClock()

    @Provides
    @Singleton
    fun provideLearningMemoryEngine(): LearningMemoryEngine = LearningMemoryEngine()

    @Provides
    @Singleton
    fun provideReviewEngine(): ReviewEngine = ReviewEngine()

    @Provides
    @Singleton
    fun provideProgressEngine(): ProgressEngine = ProgressEngine()

    @Provides
    @Singleton
    fun provideRecommendationEngine(): RecommendationEngine = RecommendationEngine()

    @Provides
    @Singleton
    fun provideKnowledgeEngine(): KnowledgeEngine = KnowledgeEngine()

    @Provides
    @Singleton
    fun provideFeedEngine(): FeedEngine = FeedEngine()

    @Provides
    @Singleton
    fun providePackageValidator(
        packageRepository: KnowledgePackageRepository,
        knowledgeRepository: KnowledgeRepository
    ): PackageValidator = PackageValidator(packageRepository, knowledgeRepository)

    @Provides
    @Singleton
    fun providePackageImporter(
        packageRepository: KnowledgePackageRepository,
        knowledgeRepository: KnowledgeRepository,
        quizRepository: QuizRepository,
        validator: PackageValidator
    ): PackageImporter = PackageImporter(packageRepository, knowledgeRepository, quizRepository, validator)

    @Provides
    @Singleton
    fun provideBuildDailyFeedWorkflow(
        memoryRepository: LearningMemoryRepository,
        roadmapRepository: RoadmapRepository,
        profileRepository: UserProfileRepository,
        packageRepository: KnowledgePackageRepository,
        sessionRepository: LearningSessionRepository,
        reviewEngine: ReviewEngine,
        progressEngine: ProgressEngine,
        recommendationEngine: RecommendationEngine,
        knowledgeEngine: KnowledgeEngine,
        feedEngine: FeedEngine,
        clock: Clock
    ): BuildDailyFeedWorkflow = BuildDailyFeedWorkflow(
        memoryRepository, roadmapRepository, profileRepository, packageRepository,
        sessionRepository, reviewEngine, progressEngine, recommendationEngine,
        knowledgeEngine, feedEngine, clock
    )

    @Provides
    @Singleton
    fun provideCompleteLearningSessionWorkflow(
        memoryRepository: LearningMemoryRepository,
        roadmapRepository: RoadmapRepository,
        profileRepository: UserProfileRepository,
        sessionRepository: LearningSessionRepository,
        progressEngine: ProgressEngine,
        clock: Clock
    ): CompleteLearningSessionWorkflow = CompleteLearningSessionWorkflow(
        memoryRepository, roadmapRepository, profileRepository, sessionRepository, progressEngine, clock
    )

    @Provides
    @Singleton
    fun provideFinishReviewWorkflow(
        memoryRepository: LearningMemoryRepository,
        memoryEngine: LearningMemoryEngine,
        clock: Clock
    ): FinishReviewWorkflow = FinishReviewWorkflow(memoryRepository, memoryEngine, clock)

    @Provides
    @Singleton
    fun provideGenerateRecommendationWorkflow(
        memoryRepository: LearningMemoryRepository,
        roadmapRepository: RoadmapRepository,
        profileRepository: UserProfileRepository,
        packageRepository: KnowledgePackageRepository,
        reviewEngine: ReviewEngine,
        knowledgeEngine: KnowledgeEngine,
        recommendationEngine: RecommendationEngine,
        clock: Clock
    ): GenerateRecommendationWorkflow = GenerateRecommendationWorkflow(
        memoryRepository, roadmapRepository, profileRepository, packageRepository, reviewEngine, knowledgeEngine, recommendationEngine, clock
    )

    @Provides
    @Singleton
    fun provideStartQuizWorkflow(
        quizRepository: QuizRepository,
        clock: Clock
    ): StartQuizWorkflow = StartQuizWorkflow(quizRepository, clock)

    @Provides
    @Singleton
    fun provideSubmitQuizAnswerWorkflow(): SubmitQuizAnswerWorkflow = SubmitQuizAnswerWorkflow()

    @Provides
    @Singleton
    fun provideFinishQuizWorkflow(
        quizRepository: QuizRepository,
        roadmapRepository: RoadmapRepository,
        memoryRepository: LearningMemoryRepository,
        profileRepository: UserProfileRepository,
        sessionRepository: LearningSessionRepository,
        knowledgePackageRepository: KnowledgePackageRepository,
        memoryEngine: LearningMemoryEngine,
        reviewEngine: ReviewEngine,
        progressEngine: ProgressEngine,
        recommendationEngine: RecommendationEngine,
        clock: Clock
    ): FinishQuizWorkflow = FinishQuizWorkflow(
        quizRepository, roadmapRepository, memoryRepository, profileRepository, sessionRepository,
        knowledgePackageRepository, memoryEngine, reviewEngine, progressEngine, recommendationEngine, clock
    )

    @Provides
    @Singleton
    fun provideImportPackageWorkflow(
        importer: PackageImporter
    ): ImportPackageWorkflow = ImportPackageWorkflow(importer)

    @Provides
    @Singleton
    fun provideLoadKnowledgeCardWorkflow(
        knowledgeRepository: KnowledgeRepository
    ): LoadKnowledgeCardWorkflow = LoadKnowledgeCardWorkflow(knowledgeRepository)

    @Provides
    @Singleton
    fun provideLoadUserProfileWorkflow(
        profileRepository: UserProfileRepository
    ): LoadUserProfileWorkflow = LoadUserProfileWorkflow(profileRepository)

    @Provides
    @Singleton
    fun provideInitializeApplicationWorkflow(
        profileRepository: UserProfileRepository,
        roadmapRepository: RoadmapRepository,
        memoryRepository: LearningMemoryRepository,
        importer: PackageImporter,
        clock: Clock
    ): InitializeApplicationWorkflow = InitializeApplicationWorkflow(
        profileRepository, roadmapRepository, memoryRepository, importer, clock
    )
}
