package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.Clock
import com.learnwitme.core.domain.service.PackageImporter

class InitializeApplicationWorkflow(
    private val profileRepository: UserProfileRepository,
    private val roadmapRepository: RoadmapRepository,
    private val memoryRepository: LearningMemoryRepository,
    private val importer: PackageImporter,
    private val clock: Clock
) {
    suspend fun execute(
        userId: UserId,
        defaultPackageManifestJsonProvider: () -> PackageManifest
    ) {
        val existingProfile = profileRepository.getUserProfile(userId)
        if (existingProfile != null) {
            return
        }

        val manifest = defaultPackageManifestJsonProvider()
        val importSummary = importer.import(manifest)
        if (importSummary.status != "SUCCESS") {
            throw IllegalStateException("Failed to import default package")
        }

        val roadmapUnits = manifest.units.map { unit ->
            LearningUnit(
                id = unit.id,
                title = unit.title,
                cardId = unit.cardId,
                quizId = unit.quizId,
                estimatedMinutes = unit.estimatedMinutes,
                topicCode = unit.topicCode
            )
        }
        val defaultRoadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_default"),
            goal = LearningGoal(description = "Become a Kotlin developer", careerPathCode = "cp_kotlin_dev", targetLevel = DifficultyLevel.MID),
            units = roadmapUnits,
            status = RoadmapStatus.ACTIVE,
            completedUnitIds = emptySet()
        )
        roadmapRepository.saveRoadmap(userId, defaultRoadmap)

        val defaultMemory = LearningMemory(
            id = LearningMemoryId("mem_${userId.value}"),
            userId = userId,
            records = emptyList()
        )
        memoryRepository.saveMemory(defaultMemory)
    }
}
