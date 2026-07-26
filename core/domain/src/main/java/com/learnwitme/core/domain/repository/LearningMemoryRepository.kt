package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing LearningMemory aggregate roots.
 */
interface LearningMemoryRepository {
    suspend fun getMemory(id: LearningMemoryId): LearningMemory?
    suspend fun getMemoryByUserId(userId: UserId): LearningMemory?
    suspend fun saveMemory(memory: LearningMemory)
}
