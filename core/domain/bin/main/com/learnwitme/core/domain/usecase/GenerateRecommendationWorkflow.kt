package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

class GenerateRecommendationWorkflow(
    private val memoryRepository: LearningMemoryRepository,
    private val roadmapRepository: RoadmapRepository,
    private val profileRepository: UserProfileRepository,
    private val packageRepository: KnowledgePackageRepository,
    private val reviewEngine: ReviewEngine,
    private val knowledgeEngine: KnowledgeEngine,
    private val recommendationEngine: RecommendationEngine,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        roadmapId: LearningRoadmapId,
        packageId: KnowledgePackageId,
        reviewPolicy: ReviewPolicy = ReviewPolicy(),
        knowledgePolicy: KnowledgePolicy = KnowledgePolicy(),
        recommendationPolicy: RecommendationPolicy = RecommendationPolicy()
    ): RecommendationDecision {
        val memory = memoryRepository.getMemoryByUserId(userId)
            ?: throw IllegalStateException("Memory not found for user \${userId.value}")
        val profile = profileRepository.getUserProfile(userId)
            ?: throw IllegalStateException("Profile not found for user \${userId.value}")
        val roadmap = roadmapRepository.getRoadmap(roadmapId)
            ?: throw IllegalStateException("Roadmap not found: \${roadmapId.value}")
        val pkg = packageRepository.getPackage(packageId)
            ?: throw IllegalStateException("Package not found: \${packageId.value}")
            
        val reviewQueue = reviewEngine.determineQueue(memory, clock, reviewPolicy)
        val knowledgeDecision = knowledgeEngine.processKnowledge(pkg, roadmap, profile, knowledgePolicy)
        
        return recommendationEngine.generateRecommendation(
            roadmap, memory, reviewQueue, roadmap.goal, profile, pkg, recommendationPolicy
        )
    }
}
