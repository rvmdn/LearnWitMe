package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserProfile
import com.learnwitme.core.domain.repository.UserProfileRepository

class LoadUserProfileWorkflow(
    private val profileRepository: UserProfileRepository
) {
    suspend fun execute(userId: UserId): UserProfile? {
        return profileRepository.getUserProfile(userId)
    }
}
