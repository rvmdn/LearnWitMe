package com.learnwitme.core.storage.mapper

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.storage.local.entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// UserProfile Mapper
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = id.value,
        careerPathCode = careerPath.code,
        careerPathName = careerPath.name,
        careerPathDescription = careerPath.description,
        learningGoalJson = "{}", // To be filled later or removed? Wait, user profile doesn't have learning goal directly! It has CareerPath! Wait, in my UserProfileEntity I put learningGoalJson... ah, let me just ignore it or remove it from entity.
        preferenceJson = Json.encodeToString(PreferenceDto(preference.preferredTopics, preference.dailyLimitMinutes)),
        currentLevel = currentLevel.name,
        targetLevel = targetLevel.name,
        progressJson = Json.encodeToString(ProgressDto(progress.totalUnitsCompleted, progress.totalCardsMastered, progress.activeStreakDays, progress.lastActiveDate))
    )
}

fun UserProfileEntity.toDomain(): UserProfile {
    val prefDto = Json.decodeFromString<PreferenceDto>(preferenceJson)
    val progDto = Json.decodeFromString<ProgressDto>(progressJson)
    return UserProfile(
        id = UserId(userId),
        careerPath = CareerPath(careerPathCode, careerPathName, careerPathDescription),
        preference = LearningPreference(prefDto.preferredTopics, prefDto.dailyTimeCommitmentMinutes),
        currentLevel = DifficultyLevel.valueOf(currentLevel),
        targetLevel = DifficultyLevel.valueOf(targetLevel),
        progress = LearningProgress(progDto.totalUnitsCompleted, progDto.totalCardsMastered, progDto.activeStreakDays, progDto.lastActiveDate)
    )
}

// Memory Mapper
fun LearningMemory.toEntity(): LearningMemoryEntity = LearningMemoryEntity(id = id.value, userId = userId.value)
fun MemoryRecord.toEntity(memoryId: String): MemoryRecordEntity {
    val reviewsDto = reviews.map { MemoryReviewDto(it.timestamp, it.accuracy, it.hintUsed, it.confidenceRating) }
    return MemoryRecordEntity(
        memoryId = memoryId,
        cardId = cardId.value,
        state = state.name,
        retention = retention,
        stability = stability,
        confidence = confidence,
        reviewsJson = Json.encodeToString(reviewsDto)
    )
}
fun LearningMemoryEntity.toDomain(records: List<MemoryRecordEntity>): LearningMemory {
    val domainRecords = records.map {
        val dtos = Json.decodeFromString<List<MemoryReviewDto>>(it.reviewsJson)
        val reviews = dtos.map { dto -> MemoryReview(dto.timestamp, dto.accuracy, dto.hintUsed, dto.confidenceRating) }
        MemoryRecord(KnowledgeCardId(it.cardId), MemoryState.valueOf(it.state), it.retention, it.stability, it.confidence, reviews)
    }
    return LearningMemory(LearningMemoryId(id), UserId(userId), domainRecords)
}

// Roadmap Mapper
fun LearningRoadmap.toEntity(userId: String): LearningRoadmapEntity {
    val goalDto = LearningGoalDto(goal.description, goal.careerPathCode, goal.targetLevel.name)
    return LearningRoadmapEntity(
        id = id.value,
        userId = userId,
        goalJson = Json.encodeToString(goalDto),
        status = status.name,
        completedUnitIdsJson = Json.encodeToString(completedUnitIds.map { it.value })
    )
}
fun LearningUnit.toEntity(roadmapId: String): LearningUnitEntity {
    return LearningUnitEntity(roadmapId, id.value, title, cardId.value, quizId?.value, estimatedMinutes, topicCode)
}
fun LearningRoadmapEntity.toDomain(units: List<LearningUnitEntity>): LearningRoadmap {
    val goalDto = Json.decodeFromString<LearningGoalDto>(goalJson)
    val completedList = Json.decodeFromString<List<String>>(completedUnitIdsJson)
    val domainUnits = units.map { LearningUnit(LearningUnitId(it.id), it.title, KnowledgeCardId(it.cardId), it.parentId?.let { p -> QuizId(p) }, it.durationMinutes, it.topicCode) }
    return LearningRoadmap(
        id = LearningRoadmapId(id),
        goal = LearningGoal(goalDto.title, goalDto.careerPathCode, DifficultyLevel.valueOf(goalDto.targetLevel)),
        units = domainUnits,
        status = RoadmapStatus.valueOf(status),
        completedUnitIds = completedList.map { LearningUnitId(it) }.toSet()
    )
}

// Session Mapper
fun LearningSession.toEntity(userId: String): LearningSessionEntity {
    return LearningSessionEntity(id.value, userId, startTime, endTime, Json.encodeToString(completedUnitIds.map { it.value }))
}
fun LearningSessionEntity.toDomain(): LearningSession {
    val ids = Json.decodeFromString<List<String>>(completedUnitIdsJson)
    return LearningSession(LearningSessionId(id), startTime, endTime, ids.map { LearningUnitId(it) })
}

// KnowledgePackage Mapper
fun KnowledgePackage.toEntity(): KnowledgePackageEntity {
    return KnowledgePackageEntity(id.value, title, description, version.toString(), status == PackageStatus.PUBLISHED)
}
fun KnowledgeCard.toEntity(packageId: String): KnowledgeCardEntity {
    val refs = reference.map { ReferenceDto(it.title, it.url) }
    val tagsDto = tags.map { TagDto(it.name) }
    return KnowledgeCardEntity(
        id = id.value,
        packageId = packageId,
        title = title,
        summary = summary,
        whyItMatters = whyItMatters,
        realWorldExample = realWorldExample,
        commonMistakes = commonMistakes,
        whenNotToUse = whenNotToUse,
        bestPractice = bestPractice,
        referencesJson = Json.encodeToString(refs),
        officialDocumentationLink = officialDocumentationLink,
        difficulty = difficulty.name,
        estimatedReadingTimeSeconds = estimatedReadingTimeSeconds,
        careerImpact = careerImpact,
        tagsJson = Json.encodeToString(tagsDto),
        prerequisiteCardIdsJson = Json.encodeToString(prerequisiteCardIds.map { it.value }),
        relatedTopicCodesJson = Json.encodeToString(relatedTopicCodes)
    )
}
fun KnowledgePackageEntity.toDomain(cards: List<KnowledgeCardEntity>): KnowledgePackage {
    val domainCards = cards.map { it.toDomain() }
    return KnowledgePackage(
        id = KnowledgePackageId(id),
        title = title,
        description = description,
        version = version.toIntOrNull() ?: 1,
        status = if (isCore) PackageStatus.PUBLISHED else PackageStatus.DRAFT,
        topics = emptyList(),
        cards = domainCards,
        dependencies = emptyList()
    )
}
fun KnowledgeCardEntity.toDomain(): KnowledgeCard {
    val refsDto = Json.decodeFromString<List<ReferenceDto>>(referencesJson)
    val tagsDto = Json.decodeFromString<List<TagDto>>(tagsJson)
    val preReqs = Json.decodeFromString<List<String>>(prerequisiteCardIdsJson)
    val topics = Json.decodeFromString<List<String>>(relatedTopicCodesJson)
    return KnowledgeCard(
        id = KnowledgeCardId(id),
        title = title,
        summary = summary,
        whyItMatters = whyItMatters,
        realWorldExample = realWorldExample,
        commonMistakes = commonMistakes,
        whenNotToUse = whenNotToUse,
        bestPractice = bestPractice,
        reference = refsDto.map { Reference(it.title, it.url) },
        officialDocumentationLink = officialDocumentationLink,
        difficulty = DifficultyLevel.valueOf(difficulty),
        estimatedReadingTimeSeconds = estimatedReadingTimeSeconds,
        careerImpact = careerImpact,
        tags = tagsDto.map { Tag(it.name) },
        prerequisiteCardIds = preReqs.map { KnowledgeCardId(it) },
        relatedTopicCodes = topics
    )
}
