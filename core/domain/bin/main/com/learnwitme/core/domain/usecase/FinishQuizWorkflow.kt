package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

class FinishQuizWorkflow(
    private val quizRepository: QuizRepository,
    private val roadmapRepository: RoadmapRepository,
    private val memoryRepository: LearningMemoryRepository,
    private val profileRepository: UserProfileRepository,
    private val sessionRepository: LearningSessionRepository,
    private val knowledgePackageRepository: KnowledgePackageRepository,
    private val memoryEngine: LearningMemoryEngine,
    private val reviewEngine: ReviewEngine,
    private val progressEngine: ProgressEngine,
    private val recommendationEngine: RecommendationEngine,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        attempt: QuizAttempt,
        progressPolicy: ProgressPolicy = ProgressPolicy(),
        reviewPolicy: ReviewPolicy = ReviewPolicy(),
        recommendationPolicy: RecommendationPolicy = RecommendationPolicy()
    ): QuizDecision {
        val result = attempt.complete(clock)
        quizRepository.saveQuizResult(result)

        val roadmap = roadmapRepository.getActiveRoadmapByUserId(userId)
            ?: throw IllegalStateException("Active roadmap not found for user ${userId.value}")
        
        val unit = roadmap.units.find { it.quizId == attempt.quiz.id }
            ?: throw IllegalStateException("Learning unit associated with quiz ${attempt.quiz.id.value} not found")

        val updatedRoadmap = if (!roadmap.completedUnitIds.contains(unit.id)) {
            roadmap.completeUnit(unit.id)
        } else {
            roadmap
        }
        roadmapRepository.saveRoadmap(updatedRoadmap)

        val memory = memoryRepository.getMemoryByUserId(userId)
            ?: throw IllegalStateException("Memory not found for user ${userId.value}")

        val quality = ReviewQuality(
            accuracy = result.score,
            hintUsed = false,
            responseTimeMs = 0L,
            confidenceRating = result.score
        )

        val memoryDecision = memoryEngine.processReview(
            memory = memory,
            cardId = unit.cardId,
            quality = quality,
            clock = clock,
            cardToTopicMapper = { cardId ->
                roadmap.units.find { it.cardId == cardId }?.topicCode ?: "default"
            }
        )
        memoryRepository.saveMemory(memoryDecision.updatedMemory)

        val profile = profileRepository.getUserProfile(userId)
            ?: throw IllegalStateException("Profile not found for user ${userId.value}")
        
        val sessions = sessionRepository.getSessions(userId)
        
        val progressDecision = progressEngine.calculateProgress(
            roadmap = updatedRoadmap,
            memory = memoryDecision.updatedMemory,
            sessions = sessions,
            profile = profile,
            clock = clock,
            policy = progressPolicy
        )
        profileRepository.saveUserProfile(progressDecision.updatedProfile)

        val activePackages = knowledgePackageRepository.getActivePackages()
        val targetPackage = activePackages.find { pkg -> pkg.cards.any { it.id == unit.cardId } }
            ?: throw IllegalStateException("No active package contains card ${unit.cardId.value}")

        val reviewQueue = reviewEngine.determineQueue(
            memory = memoryDecision.updatedMemory,
            clock = clock,
            policy = reviewPolicy
        )

        val recommendationDecision = recommendationEngine.generateRecommendation(
            roadmap = updatedRoadmap,
            memory = memoryDecision.updatedMemory,
            reviewQueue = reviewQueue,
            goal = updatedRoadmap.goal,
            profile = progressDecision.updatedProfile,
            knowledgePackage = targetPackage,
            policy = recommendationPolicy
        )

        return QuizDecision(
            result = result,
            updatedProgress = progressDecision.summary,
            updatedRecommendation = recommendationDecision
        )
    }
}
