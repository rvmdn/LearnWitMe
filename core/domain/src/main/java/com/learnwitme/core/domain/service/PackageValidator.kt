package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.KnowledgePackageRepository
import com.learnwitme.core.domain.repository.KnowledgeRepository

class PackageValidator(
    private val packageRepository: KnowledgePackageRepository,
    private val knowledgeRepository: KnowledgeRepository
) {
    suspend fun validate(manifest: PackageManifest): ValidationResult {
        val errors = mutableListOf<String>()

        // 0. Metadata Required Fields Validation
        if (manifest.metadata.title.isBlank()) {
            errors.add("Package title cannot be blank")
        }
        if (manifest.metadata.id.value.isBlank()) {
            errors.add("Package ID cannot be blank")
        }

        // 1. Checksum Validation
        val calculatedChecksum = calculateChecksum(manifest)
        if (calculatedChecksum.value != manifest.metadata.checksum.value) {
            errors.add("Invalid checksum: expected ${manifest.metadata.checksum.value} but calculated ${calculatedChecksum.value}")
        }

        // 2. Duplicate ID Validation
        val duplicateTopics = manifest.topics.map { it.code }.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicateTopics.isNotEmpty()) {
            errors.add("Duplicate Topic codes found: $duplicateTopics")
        }

        val duplicateCards = manifest.cards.map { it.id.value }.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicateCards.isNotEmpty()) {
            errors.add("Duplicate KnowledgeCard IDs found: $duplicateCards")
        }

        val duplicateUnits = manifest.units.map { it.id.value }.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicateUnits.isNotEmpty()) {
            errors.add("Duplicate LearningUnit IDs found: $duplicateUnits")
        }

        val duplicateQuizzes = manifest.quizzes.map { it.id.value }.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicateQuizzes.isNotEmpty()) {
            errors.add("Duplicate Quiz IDs found: $duplicateQuizzes")
        }

        // 3. Version Compatibility Validation
        val existingPkg = packageRepository.getPackage(manifest.metadata.id)
        if (existingPkg != null) {
            val manifestVersionInt = manifest.metadata.version.toInt()
            if (manifestVersionInt < existingPkg.version) {
                errors.add("Invalid version: Cannot import package version ${manifest.metadata.version} which is older than existing version ${existingPkg.version.toPackageVersion()}")
            }
        }

        // 4. Package Dependencies Validation
        for (depId in manifest.dependencies) {
            val depPkg = packageRepository.getPackage(depId)
            if (depPkg == null) {
                errors.add("Missing required package dependency: ${depId.value}")
            }
        }

        // 5. Missing Prerequisite & Unit Relation Validation
        val manifestCardIds = manifest.cards.map { it.id }.toSet()
        for (card in manifest.cards) {
            for (preId in card.prerequisiteCardIds) {
                if (!manifestCardIds.contains(preId)) {
                    val dbCard = knowledgeRepository.getCard(preId)
                    if (dbCard == null) {
                        errors.add("Missing prerequisite dependency: Card ${card.id.value} requires prerequisite Card ${preId.value} which is not in this package or database")
                    }
                }
            }
        }

        for (unit in manifest.units) {
            if (!manifestCardIds.contains(unit.cardId)) {
                val dbCard = knowledgeRepository.getCard(unit.cardId)
                if (dbCard == null) {
                    errors.add("Missing unit dependency: LearningUnit ${unit.id.value} references card ${unit.cardId.value} which is not in this package or database")
                }
            }
        }

        // 6. Circular Dependency Validation
        if (detectCircularDependencies(manifest.cards)) {
            errors.add("Circular dependency detected in KnowledgeCards")
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    private suspend fun detectCircularDependencies(cards: List<KnowledgeCard>): Boolean {
        val visited = mutableSetOf<KnowledgeCardId>()
        val stack = mutableSetOf<KnowledgeCardId>()

        suspend fun hasCycle(cardId: KnowledgeCardId): Boolean {
            if (stack.contains(cardId)) return true
            if (visited.contains(cardId)) return false

            visited.add(cardId)
            stack.add(cardId)

            val card = cards.find { it.id == cardId } ?: knowledgeRepository.getCard(cardId)
            if (card != null) {
                for (preId in card.prerequisiteCardIds) {
                    if (hasCycle(preId)) return true
                }
            }

            stack.remove(cardId)
            return false
        }

        for (card in cards) {
            if (hasCycle(card.id)) return true
        }
        return false
    }
}
