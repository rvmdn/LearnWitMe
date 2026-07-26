package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningSession
import com.learnwitme.core.domain.model.UserId

interface LearningSessionRepository {
    suspend fun getSessions(userId: UserId): List<LearningSession>
    suspend fun saveSession(userId: UserId, session: LearningSession)
}
