package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference as MemoryLearningPreference
import com.learnwitme.core.domain.model.memory.personalization.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

/**
 * Single Responsibility: Operationalizes personalization by transforming a learner's identity,
 * preferences, and Personalized Learning Context into a deterministic daily learning plan/feed.
 * Side-effect free read/compute operation (zero repository writes during initialization).
 */
class InitializePersonalizedDailyLearningWorkflow(
    private val buildPersonalizedLearningContextWorkflow: BuildPersonalizedLearningContextWorkflow,
    private val profileRepository: UserProfileRepository,
    private val memoryRepository: LearningMemoryRepository,
    private val roadmapRepository: RoadmapRepository,
    private val packageRepository: KnowledgePackageRepository,
    private val sessionRepository: LearningSessionRepository,
    private val reviewEngine: ReviewEngine,
    private val progressEngine: ProgressEngine,
    private val recommendationEngine: RecommendationEngine,
    private val knowledgeEngine: KnowledgeEngine,
    private val feedEngine: FeedEngine,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        roadmapId: LearningRoadmapId? = null,
        packageId: KnowledgePackageId? = null,
        personalizationPolicy: LearningPersonalizationPolicy = LearningPersonalizationPolicy.FULL,
        reviewPolicy: ReviewPolicy = ReviewPolicy(),
        progressPolicy: ProgressPolicy = ProgressPolicy(),
        recommendationPolicy: RecommendationPolicy = RecommendationPolicy(),
        knowledgePolicy: KnowledgePolicy = KnowledgePolicy(),
        feedPolicy: FeedPolicy = FeedPolicy()
    ): DailyLearningInitializationResult {
        // 1. Build Personalized Learning Context
        val contextResult = buildPersonalizedLearningContextWorkflow.execute(userId, personalizationPolicy)
        if (contextResult is PersonalizedContextResult.Incomplete) {
            return DailyLearningInitializationResult.IncompletePersonalization(contextResult)
        }

        val context = (contextResult as PersonalizedContextResult.Success).context
        val profile = profileRepository.getUserProfile(userId)
            ?: return DailyLearningInitializationResult.IncompletePersonalization(PersonalizedContextResult.Incomplete.MissingProfile())

        // 2. Resolve LearningMemory (fallback to default memory for fresh guest users)
        val memoryId = LearningMemoryId("mem_${userId.value}")
        val memory = memoryRepository.getMemory(memoryId)
            ?: memoryRepository.getMemoryByUserId(userId)
            ?: LearningMemory(
                id = memoryId,
                userId = userId,
                records = emptyList(),
                preference = MemoryLearningPreference.undetermined(nowEpochMs = clock.currentTimeMillis()),
                pattern = LearningPattern.empty(nowEpochMs = clock.currentTimeMillis())
            )

        // 3. Resolve Roadmap (fallback to active or default roadmap)
        val resolvedRoadmapId = roadmapId
            ?: roadmapRepository.getActiveRoadmapByUserId(userId)?.id
            ?: LearningRoadmapId("rm_default")
        val roadmap = roadmapRepository.getRoadmap(resolvedRoadmapId)
            ?: roadmapRepository.getActiveRoadmapByUserId(userId)

        if (roadmap == null) {
            return DailyLearningInitializationResult.ContentUnavailable(
                userId = userId,
                context = context,
                reason = "No active or default roadmap found"
            )
        }

        // 4. Resolve Knowledge Package
        val resolvedPackageId = packageId
            ?: packageRepository.getActivePackages().firstOrNull()?.id
            ?: KnowledgePackageId("pkg_default")
        val pkg = packageRepository.getPackage(resolvedPackageId)
            ?: packageRepository.getActivePackages().firstOrNull()

        if (pkg == null) {
            return DailyLearningInitializationResult.ContentUnavailable(
                userId = userId,
                context = context,
                reason = "No active knowledge package found"
            )
        }

        // 5. Evaluate Sessions & Engines
        val sessions = sessionRepository.getSessions(userId)

        val reviewQueue = reviewEngine.determineQueue(memory, clock, reviewPolicy)
        val progressDecision = progressEngine.calculateProgress(roadmap, memory, sessions, profile, clock, progressPolicy)
        val knowledgeDecision = knowledgeEngine.processKnowledge(pkg, roadmap, profile, knowledgePolicy)
        val recommendationDecision = recommendationEngine.generateRecommendation(
            roadmap, memory, reviewQueue, roadmap.goal, profile, pkg, recommendationPolicy
        )

        // 6. Compose Daily Feed
        val feedDecision = feedEngine.composeFeed(
            recommendation = recommendationDecision,
            progress = progressDecision.summary,
            reviewQueue = reviewQueue,
            knowledge = knowledgeDecision,
            memory = memory,
            profile = profile,
            policy = feedPolicy
        )

        // 7. Determine Initialization State
        val hasDueReview = reviewQueue.decisions.any { it.isOverdue || it.priority == ReviewPriority.CRITICAL || it.priority == ReviewPriority.HIGH }
        val state = when {
            hasDueReview -> DailyInitializationState.REVIEW_DUE_PRIORITIZED
            memory.records.isNotEmpty() -> DailyInitializationState.RETURNING_USER
            roadmap.id.value == "rm_default" -> DailyInitializationState.NO_HISTORY_DEFAULT_ROADMAP
            else -> DailyInitializationState.NEW_GUEST_INITIALIZED
        }


        return DailyLearningInitializationResult.Success(
            userId = userId,
            context = context,
            feedDecision = feedDecision,
            initializationState = state
        )
    }
}
