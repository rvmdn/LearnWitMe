package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.model.memory.LearningPattern
import com.learnwitme.core.domain.model.memory.LearningPreference as MemoryLearningPreference
import com.learnwitme.core.domain.model.memory.personalization.*
import com.learnwitme.core.domain.repository.LearningMemoryRepository
import com.learnwitme.core.domain.repository.UserProfileRepository
import com.learnwitme.core.domain.service.Clock

/**
 * Responsibility: Domain use case that transforms persisted UserProfile + LearningPreference state
 * and LearningMemory into a deterministic, privacy-filtered LearningPersonalizationContext.
 * Completely side-effect free (no repository writes or mutations).
 */
class BuildPersonalizedLearningContextWorkflow(
    private val profileRepository: UserProfileRepository,
    private val memoryRepository: LearningMemoryRepository,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        policy: LearningPersonalizationPolicy = LearningPersonalizationPolicy.FULL
    ): PersonalizedContextResult {
        val profile = profileRepository.getUserProfile(userId)
            ?: return PersonalizedContextResult.Incomplete.MissingProfile()

        if (profile.preference.preferredTopics.isEmpty()) {
            return PersonalizedContextResult.Incomplete.EmptyPreferredTopics()
        }

        if (profile.preference.dailyLimitMinutes <= 0) {
            return PersonalizedContextResult.Incomplete.InvalidDailyLimit()
        }

        if (profile.currentLevel.ordinal > profile.targetLevel.ordinal) {
            return PersonalizedContextResult.Incomplete.InvalidLevelCombination()
        }

        val memoryId = LearningMemoryId("mem_${userId.value}")
        val existingMemory = memoryRepository.getMemory(memoryId)
            ?: memoryRepository.getMemoryByUserId(userId)
            ?: LearningMemory(
                id = memoryId,
                userId = userId,
                records = emptyList(),
                preference = MemoryLearningPreference.undetermined(nowEpochMs = clock.currentTimeMillis()),
                pattern = LearningPattern.empty(nowEpochMs = clock.currentTimeMillis())
            )

        val activeGoals = buildList {
            if (profile.careerPath.name.isNotBlank()) {
                add(profile.careerPath.name)
            }
            addAll(profile.preference.preferredTopics)
        }.distinct()

        val context = LearningPersonalizationBuilder.build(
            memory = existingMemory,
            policy = policy,
            activeGoals = activeGoals,
            nowEpochMs = clock.currentTimeMillis()
        )

        return PersonalizedContextResult.Success(context)
    }
}
