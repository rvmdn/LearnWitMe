package com.learnwitme.core.domain.service

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*

class PackageImporter(
    private val packageRepository: KnowledgePackageRepository,
    private val knowledgeRepository: KnowledgeRepository,
    private val quizRepository: QuizRepository,
    private val validator: PackageValidator
) {
    suspend fun import(manifest: PackageManifest): ImportSummary {
        val validation = validator.validate(manifest)
        if (!validation.isValid) {
            throw IllegalArgumentException("Package validation failed: ${validation.errors.joinToString("; ")}")
        }

        // Map manifest elements to Domain structures
        val pkg = KnowledgePackage(
            id = manifest.metadata.id,
            title = manifest.metadata.title,
            description = manifest.metadata.description,
            version = manifest.metadata.version.toInt(),
            status = PackageStatus.PUBLISHED,
            topics = manifest.topics,
            cards = manifest.cards,
            dependencies = manifest.dependencies
        )

        // Persist all package materials offline
        packageRepository.savePackage(pkg)
        
        for (card in manifest.cards) {
            knowledgeRepository.saveCard(card)
        }

        for (quiz in manifest.quizzes) {
            quizRepository.saveQuiz(quiz)
        }
        
        return ImportSummary(
            packageId = manifest.metadata.id,
            version = manifest.metadata.version,
            topicsImported = manifest.topics.size,
            cardsImported = manifest.cards.size,
            unitsImported = manifest.units.size,
            quizzesImported = manifest.quizzes.size,
            status = "SUCCESS"
        )
    }
}
