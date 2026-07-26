package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.UserProfile
import com.learnwitme.core.domain.model.UserId

/**
 * Responsibility: Repository contract for managing UserProfile aggregate roots.
 */
interface UserProfileRepository {
    suspend fun getUserProfile(userId: UserId): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
}
