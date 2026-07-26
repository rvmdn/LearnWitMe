package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningProgress
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing LearningProgress values.
 */
interface ProgressRepository {
    suspend fun getProgress(userId: UserId): LearningProgress?
    suspend fun saveProgress(userId: UserId, progress: LearningProgress)
}
