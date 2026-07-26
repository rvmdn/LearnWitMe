package com.learnwitme.core.storage.mapper

import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgeContent
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.PackageStatus
import com.learnwitme.core.domain.model.Reference
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.Tag
import com.learnwitme.core.storage.local.entity.KnowledgeCardEntity
import com.learnwitme.core.storage.local.entity.KnowledgePackageEntity
import com.learnwitme.core.storage.local.entity.ReferenceDto
import com.learnwitme.core.storage.local.entity.SkillDto
import com.learnwitme.core.storage.local.entity.TagDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Responsibility: Pure, deterministic bidirectional mapping object converting between static
 * Knowledge domain models (KnowledgePackage, KnowledgeCard) and Room persistence entities.
 */
object KnowledgeMapper {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun toEntity(pkg: KnowledgePackage): KnowledgePackageEntity {
        return KnowledgePackageEntity(
            id = pkg.id.value,
            title = pkg.title,
            description = pkg.description,
            version = pkg.version.toString(),
            checksum = pkg.checksum,
            status = pkg.status.name,
            isCore = pkg.isCore
        )
    }

    fun toDomain(entity: KnowledgePackageEntity, cards: List<KnowledgeCardEntity> = emptyList()): KnowledgePackage {
        return KnowledgePackage(
            id = KnowledgePackageId(entity.id),
            title = entity.title,
            description = entity.description,
            version = entity.version.toIntOrNull() ?: 1,
            checksum = entity.checksum,
            isCore = entity.isCore,
            status = runCatching { PackageStatus.valueOf(entity.status) }.getOrDefault(PackageStatus.DRAFT),
            topics = emptyList(),
            cards = cards.map { toDomain(it) },
            dependencies = emptyList()
        )
    }

    fun toEntity(card: KnowledgeCard, packageId: String? = null): KnowledgeCardEntity {
        val refsDto = card.references.map { ReferenceDto(it.title, it.url) }
        val tagsDto = card.tags.map { TagDto(it.name) }
        val skillsDto = card.skills.map { SkillDto(it.code, it.name, it.category) }

        return KnowledgeCardEntity(
            id = card.id.value,
            packageId = packageId ?: card.packageId.value,
            title = card.content.title,
            summary = card.content.summary,
            whyItMatters = card.content.whyItMatters,
            realWorldExample = card.content.realWorldExample,
            commonMistakes = card.content.commonMistakes,
            whenNotToUse = card.content.whenNotToUse,
            bestPractice = card.content.bestPractice,
            referencesJson = json.encodeToString(refsDto),
            officialDocumentationLink = card.content.officialDocumentationLink,
            difficulty = card.difficulty.name,
            estimatedReadingTimeSeconds = card.estimatedReadingTimeSeconds,
            careerImpact = card.careerImpact,
            skillsJson = json.encodeToString(skillsDto),
            tagsJson = json.encodeToString(tagsDto),
            prerequisiteCardIdsJson = json.encodeToString(card.prerequisiteCardIds.map { it.value }),
            relatedTopicCodesJson = json.encodeToString(card.relatedTopicCodes)
        )
    }

    fun toDomain(entity: KnowledgeCardEntity): KnowledgeCard {
        val refsDto = runCatching { json.decodeFromString<List<ReferenceDto>>(entity.referencesJson) }.getOrDefault(emptyList())
        val tagsDto = runCatching { json.decodeFromString<List<TagDto>>(entity.tagsJson) }.getOrDefault(emptyList())
        val skillsDto = runCatching { json.decodeFromString<List<SkillDto>>(entity.skillsJson) }.getOrDefault(emptyList())
        val preReqs = runCatching { json.decodeFromString<List<String>>(entity.prerequisiteCardIdsJson) }.getOrDefault(emptyList())
        val topics = runCatching { json.decodeFromString<List<String>>(entity.relatedTopicCodesJson) }.getOrDefault(emptyList())

        return KnowledgeCard(
            id = KnowledgeCardId(entity.id),
            packageId = KnowledgePackageId(entity.packageId),
            content = KnowledgeContent(
                title = entity.title,
                summary = entity.summary,
                whyItMatters = entity.whyItMatters,
                realWorldExample = entity.realWorldExample,
                commonMistakes = entity.commonMistakes,
                whenNotToUse = entity.whenNotToUse,
                bestPractice = entity.bestPractice,
                officialDocumentationLink = entity.officialDocumentationLink
            ),
            difficulty = runCatching { DifficultyLevel.valueOf(entity.difficulty) }.getOrDefault(DifficultyLevel.BEGINNER),
            estimatedReadingTimeSeconds = entity.estimatedReadingTimeSeconds,
            careerImpact = entity.careerImpact,
            skills = skillsDto.map { Skill(it.code, it.name, it.category) },
            references = refsDto.map { Reference(it.title, it.url) },
            tags = tagsDto.map { Tag(it.name) },
            prerequisiteCardIds = preReqs.map { KnowledgeCardId(it) },
            relatedTopicCodes = topics
        )
    }
}
