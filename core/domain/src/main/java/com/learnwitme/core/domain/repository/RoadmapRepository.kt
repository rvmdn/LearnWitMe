package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningRoadmap
import com.learnwitme.core.domain.model.LearningRoadmapId
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing LearningRoadmap aggregate roots.
 */
interface RoadmapRepository {
    suspend fun getRoadmap(id: LearningRoadmapId): LearningRoadmap?
    suspend fun getActiveRoadmapByUserId(userId: UserId): LearningRoadmap?
    suspend fun saveRoadmap(roadmap: LearningRoadmap)
}
