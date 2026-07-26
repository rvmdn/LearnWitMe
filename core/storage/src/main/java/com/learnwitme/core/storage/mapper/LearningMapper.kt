package com.learnwitme.core.storage.mapper

import com.learnwitme.core.domain.model.Competency
import com.learnwitme.core.domain.model.CompetencyLevel
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.ReviewSchedule
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.UserKnowledge
import com.learnwitme.core.storage.local.entity.CompetencyDto
import com.learnwitme.core.storage.local.entity.ReviewScheduleEntity
import com.learnwitme.core.storage.local.entity.UserKnowledgeEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Responsibility: Pure, deterministic bidirectional mapping object converting between dynamic Learner
 * domain models (UserKnowledge, ReviewSchedule) and Room persistence entities.
 */
object LearningMapper {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun toEntity(userKnowledge: UserKnowledge): UserKnowledgeEntity {
        val competenciesDto = userKnowledge.competencies.mapValues { (_, comp) ->
            CompetencyDto(comp.level.name, comp.score)
        }
        return UserKnowledgeEntity(
            userId = userKnowledge.id.value,
            competenciesJson = json.encodeToString(competenciesDto),
            activeCardIdsJson = json.encodeToString(userKnowledge.activeCardIds.map { it.value }),
            masteredCardIdsJson = json.encodeToString(userKnowledge.masteredCardIds.map { it.value })
        )
    }

    fun toDomain(entity: UserKnowledgeEntity): UserKnowledge {
        val compMapDto = runCatching {
            json.decodeFromString<Map<String, CompetencyDto>>(entity.competenciesJson)
        }.getOrDefault(emptyMap())
        val activeIds = runCatching {
            json.decodeFromString<List<String>>(entity.activeCardIdsJson)
        }.getOrDefault(emptyList())
        val masteredIds = runCatching {
            json.decodeFromString<List<String>>(entity.masteredCardIdsJson)
        }.getOrDefault(emptyList())

        val competenciesDomain = compMapDto.mapValues { (_, dto) ->
            Competency(
                level = runCatching { CompetencyLevel.valueOf(dto.level) }.getOrDefault(CompetencyLevel.NOVICE),
                score = dto.score
            )
        }

        return UserKnowledge(
            id = UserId(entity.userId),
            competencies = competenciesDomain,
            activeCardIds = activeIds.map { KnowledgeCardId(it) }.toSet(),
            masteredCardIds = masteredIds.map { KnowledgeCardId(it) }.toSet()
        )
    }

    fun toEntity(userId: UserId, schedule: ReviewSchedule): ReviewScheduleEntity {
        return ReviewScheduleEntity(
            userId = userId.value,
            cardId = schedule.cardId.value,
            intervalDays = schedule.intervalDays,
            nextReviewTimestamp = schedule.nextReviewTimestamp,
            easeFactor = schedule.easeFactor,
            repetitions = schedule.repetitions
        )
    }

    fun toDomain(entity: ReviewScheduleEntity): ReviewSchedule {
        return ReviewSchedule(
            cardId = KnowledgeCardId(entity.cardId),
            intervalDays = entity.intervalDays,
            nextReviewTimestamp = entity.nextReviewTimestamp,
            easeFactor = entity.easeFactor,
            repetitions = entity.repetitions
        )
    }
}
