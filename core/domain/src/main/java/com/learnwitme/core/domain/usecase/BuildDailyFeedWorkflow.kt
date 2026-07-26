package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

class BuildDailyFeedWorkflow(
    private val memoryRepository: LearningMemoryRepository,
    private val roadmapRepository: RoadmapRepository,
    private val profileRepository: UserProfileRepository,
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
        reviewPolicy: ReviewPolicy = ReviewPolicy(),
        progressPolicy: ProgressPolicy = ProgressPolicy(),
        recommendationPolicy: RecommendationPolicy = RecommendationPolicy(),
        knowledgePolicy: KnowledgePolicy = KnowledgePolicy(),
        feedPolicy: FeedPolicy = FeedPolicy()
    ): FeedDecision {
        val memory = memoryRepository.getMemoryByUserId(userId)
            ?: throw IllegalStateException("Memory not found for user ${userId.value}")
        val profile = profileRepository.getUserProfile(userId)
            ?: throw IllegalStateException("Profile not found for user ${userId.value}")
            
        val resolvedRoadmapId = roadmapId ?: roadmapRepository.getActiveRoadmapByUserId(userId)?.id
            ?: throw IllegalStateException("No active roadmap found for user ${userId.value}")
        val roadmap = roadmapRepository.getRoadmap(resolvedRoadmapId)
            ?: throw IllegalStateException("Roadmap not found: ${resolvedRoadmapId.value}")
            
        val resolvedPackageId = packageId ?: packageRepository.getActivePackages().firstOrNull()?.id
            ?: throw IllegalStateException("No active knowledge package found")
        val pkg = packageRepository.getPackage(resolvedPackageId)
            ?: throw IllegalStateException("Package not found: ${resolvedPackageId.value}")
            
        val sessions = sessionRepository.getSessions(userId)

        val reviewQueue = reviewEngine.determineQueue(memory, clock, reviewPolicy)
        val progressDecision = progressEngine.calculateProgress(roadmap, memory, sessions, profile, clock, progressPolicy)
        val knowledgeDecision = knowledgeEngine.processKnowledge(pkg, roadmap, profile, knowledgePolicy)
        val recommendationDecision = recommendationEngine.generateRecommendation(
            roadmap, memory, reviewQueue, roadmap.goal, profile, pkg, recommendationPolicy
        )

        return feedEngine.composeFeed(
            recommendation = recommendationDecision,
            progress = progressDecision.summary,
            reviewQueue = reviewQueue,
            knowledge = knowledgeDecision,
            memory = memory,
            profile = profile,
            policy = feedPolicy
        )
    }
}
