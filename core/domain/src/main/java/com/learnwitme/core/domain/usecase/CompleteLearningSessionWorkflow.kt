package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*

class CompleteLearningSessionWorkflow(
    private val memoryRepository: LearningMemoryRepository,
    private val roadmapRepository: RoadmapRepository,
    private val profileRepository: UserProfileRepository,
    private val sessionRepository: LearningSessionRepository,
    private val progressEngine: ProgressEngine,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        roadmapId: LearningRoadmapId,
        session: LearningSession,
        progressPolicy: ProgressPolicy = ProgressPolicy()
    ) {
        val roadmap = roadmapRepository.getRoadmap(roadmapId)
            ?: throw IllegalStateException("Roadmap not found: \${roadmapId.value}")
        
        var updatedRoadmap = roadmap
        for (unitId in session.completedUnitIds) {
            updatedRoadmap = updatedRoadmap.completeUnit(unitId)
        }
        
        roadmapRepository.saveRoadmap(userId, updatedRoadmap)
        sessionRepository.saveSession(userId, session)
        
        val sessions = sessionRepository.getSessions(userId)
        val memory = memoryRepository.getMemoryByUserId(userId)
            ?: throw IllegalStateException("Memory not found for user \${userId.value}")
        val profile = profileRepository.getUserProfile(userId)
            ?: throw IllegalStateException("Profile not found for user \${userId.value}")
            
        val progressDecision = progressEngine.calculateProgress(
            updatedRoadmap, memory, sessions, profile, clock, progressPolicy
        )
        
        profileRepository.saveUserProfile(progressDecision.updatedProfile)
    }
}
