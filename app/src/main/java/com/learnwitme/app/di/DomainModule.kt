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
        packageRepository: KnowledgePackageRepository,
        importer: PackageImporter,
        clock: Clock
    ): InitializeApplicationWorkflow = InitializeApplicationWorkflow(
        profileRepository, roadmapRepository, memoryRepository, packageRepository, importer, clock
    )

    @Provides
    @Singleton
    fun provideInitializeGuestUserWorkflow(
        profileRepository: UserProfileRepository
    ): InitializeGuestUserWorkflow = InitializeGuestUserWorkflow(profileRepository)

    @Provides
    @Singleton
    fun provideUpdateLearningPreferenceWorkflow(
        profileRepository: UserProfileRepository
    ): UpdateLearningPreferenceWorkflow = UpdateLearningPreferenceWorkflow(profileRepository)

    @Provides
    @Singleton
    fun provideBuildPersonalizedLearningContextWorkflow(
        profileRepository: UserProfileRepository,
        memoryRepository: LearningMemoryRepository,
        clock: Clock
    ): BuildPersonalizedLearningContextWorkflow = BuildPersonalizedLearningContextWorkflow(
        profileRepository, memoryRepository, clock
    )

    @Provides
    @Singleton
    fun provideInitializePersonalizedDailyLearningWorkflow(
        buildPersonalizedLearningContextWorkflow: BuildPersonalizedLearningContextWorkflow,
        profileRepository: UserProfileRepository,
        memoryRepository: LearningMemoryRepository,
        roadmapRepository: RoadmapRepository,
        packageRepository: KnowledgePackageRepository,
        sessionRepository: LearningSessionRepository,
        reviewEngine: ReviewEngine,
        progressEngine: ProgressEngine,
        recommendationEngine: RecommendationEngine,
        knowledgeEngine: KnowledgeEngine,
        feedEngine: FeedEngine,
        clock: Clock
    ): InitializePersonalizedDailyLearningWorkflow = InitializePersonalizedDailyLearningWorkflow(
        buildPersonalizedLearningContextWorkflow, profileRepository, memoryRepository,
        roadmapRepository, packageRepository, sessionRepository, reviewEngine, progressEngine,
        recommendationEngine, knowledgeEngine, feedEngine, clock
    )

    @Provides
    @Singleton
    fun provideDetermineUserStartupStateWorkflow(
        profileRepository: UserProfileRepository,
        buildPersonalizedLearningContextWorkflow: BuildPersonalizedLearningContextWorkflow
    ): DetermineUserStartupStateWorkflow = DetermineUserStartupStateWorkflow(
        profileRepository, buildPersonalizedLearningContextWorkflow
    )






    @Provides
    @Singleton
    fun provideDispatcherProvider(): com.learnwitme.core.application.dispatcher.DispatcherProvider =
        com.learnwitme.core.application.dispatcher.DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideSpacedRepetitionEngine(): com.learnwitme.core.domain.service.SpacedRepetitionEngine =
        com.learnwitme.core.domain.service.SpacedRepetitionEngine()

    @Provides
    @Singleton
    fun provideGenerateDailyRecommendationUseCase(
        knowledgeRepository: KnowledgeRepository,
        learningRepository: LearningRepository,
        recommendationRepository: RecommendationRepository,
        recommendationEngine: RecommendationEngine
    ): GenerateDailyRecommendationUseCase = GenerateDailyRecommendationUseCase(
        knowledgeRepository, learningRepository, recommendationRepository, recommendationEngine
    )

    @Provides
    @Singleton
    fun provideStartLearningUseCase(
        learningRepository: LearningRepository
    ): StartLearningUseCase = StartLearningUseCase(learningRepository)

    @Provides
    @Singleton
    fun provideCompleteReviewUseCase(
        learningRepository: LearningRepository,
        spacedRepetitionEngine: com.learnwitme.core.domain.service.SpacedRepetitionEngine
    ): CompleteReviewUseCase = CompleteReviewUseCase(learningRepository, spacedRepetitionEngine)

    @Provides
    @Singleton
    fun providePackageValidationEngine(): com.learnwitme.core.domain.service.PackageValidationEngine =
        com.learnwitme.core.domain.service.PackageValidationEngine()

    @Provides
    @Singleton
    fun provideDifficultyEvaluator(): com.learnwitme.core.domain.service.DifficultyEvaluator =
        com.learnwitme.core.domain.service.DifficultyEvaluator()

    @Provides
    @Singleton
    fun provideImportKnowledgePackageUseCase(
        knowledgeRepository: KnowledgeRepository,
        validationEngine: com.learnwitme.core.domain.service.PackageValidationEngine
    ): ImportKnowledgePackageUseCase = ImportKnowledgePackageUseCase(knowledgeRepository, validationEngine)

    @Provides
    @Singleton
    fun providePublishKnowledgePackageUseCase(
        knowledgeRepository: KnowledgeRepository
    ): PublishKnowledgePackageUseCase = PublishKnowledgePackageUseCase(knowledgeRepository)

    @Provides
    @Singleton
    fun provideFindKnowledgeCardUseCase(
        knowledgeRepository: KnowledgeRepository,
        learningRepository: LearningRepository,
        difficultyEvaluator: com.learnwitme.core.domain.service.DifficultyEvaluator
    ): FindKnowledgeCardUseCase = FindKnowledgeCardUseCase(knowledgeRepository, learningRepository, difficultyEvaluator)

    @Provides
    @Singleton
    fun provideHomeApplicationService(
        generateDailyRecommendationUseCase: GenerateDailyRecommendationUseCase,
        learningRepository: LearningRepository,
        recommendationRepository: RecommendationRepository,
        dispatcherProvider: com.learnwitme.core.application.dispatcher.DispatcherProvider
    ): com.learnwitme.core.application.service.HomeApplicationService =
        com.learnwitme.core.application.service.HomeApplicationService(
            generateDailyRecommendationUseCase, learningRepository, recommendationRepository, dispatcherProvider
        )

    @Provides
    @Singleton
    fun provideLearningApplicationService(
        startLearningUseCase: StartLearningUseCase,
        completeReviewUseCase: CompleteReviewUseCase,
        learningRepository: LearningRepository,
        dispatcherProvider: com.learnwitme.core.application.dispatcher.DispatcherProvider
    ): com.learnwitme.core.application.service.LearningApplicationService =
        com.learnwitme.core.application.service.LearningApplicationService(
            startLearningUseCase, completeReviewUseCase, learningRepository, dispatcherProvider
        )

    @Provides
    @Singleton
    fun provideKnowledgeApplicationService(
        importKnowledgePackageUseCase: ImportKnowledgePackageUseCase,
        publishKnowledgePackageUseCase: PublishKnowledgePackageUseCase,
        findKnowledgeCardUseCase: FindKnowledgeCardUseCase,
        knowledgeRepository: KnowledgeRepository,
        dispatcherProvider: com.learnwitme.core.application.dispatcher.DispatcherProvider
    ): com.learnwitme.core.application.service.KnowledgeApplicationService =
        com.learnwitme.core.application.service.KnowledgeApplicationService(
            importKnowledgePackageUseCase, publishKnowledgePackageUseCase, findKnowledgeCardUseCase, knowledgeRepository, dispatcherProvider
        )

    @Provides
    @Singleton
    fun provideAIProviderRegistry(): com.learnwitme.core.domain.ai.AIProviderRegistry {
        val registry = com.learnwitme.core.domain.ai.AIProviderRegistry()
        registry.registerProvider(
            com.learnwitme.core.domain.ai.AIProviderType.MOCK,
            com.learnwitme.core.domain.ai.MockAIProvider()
        )
        return registry
    }

    @Provides
    @Singleton
    fun provideAIProviderRouter(
        registry: com.learnwitme.core.domain.ai.AIProviderRegistry
    ): com.learnwitme.core.domain.ai.AIProviderRouter =
        com.learnwitme.core.domain.ai.AIProviderRouter(registry)

    @Provides
    @Singleton
    fun provideAIManager(
        router: com.learnwitme.core.domain.ai.AIProviderRouter
    ): com.learnwitme.core.domain.ai.AIManager =
        com.learnwitme.core.domain.ai.AIManager(router)

    @Provides
    @Singleton
    fun provideAILearningInteractionPersistenceCoordinator(
        repository: AILearningInteractionRepository,
        analyticsPipeline: com.learnwitme.core.analytics.AILearningAnalyticsPipeline
    ): com.learnwitme.core.application.ai.feedback.AILearningInteractionPersistenceCoordinator {
        return com.learnwitme.core.application.ai.feedback.AILearningInteractionPersistenceCoordinator(
            repository = repository,
            onResultPersisted = { result ->
                try {
                    analyticsPipeline.processAndDispatch(result)
                } catch (_: Exception) {
                    // Analytics dispatch fault isolation: analytics failure does not fail persistence or learning
                }
            }
        )
    }

    @Provides
    @Singleton
    fun provideAILearningInteractionService(
        aiManager: com.learnwitme.core.domain.ai.AIManager,
        persistenceCoordinator: com.learnwitme.core.application.ai.feedback.AILearningInteractionPersistenceCoordinator
    ): com.learnwitme.core.application.ai.feedback.AILearningInteractionService {
        return com.learnwitme.core.application.ai.feedback.AILearningInteractionService(
            aiManager = aiManager,
            persistenceCoordinator = persistenceCoordinator
        )
    }
}
