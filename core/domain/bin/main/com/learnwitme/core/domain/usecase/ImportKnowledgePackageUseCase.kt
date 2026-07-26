package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.event.KnowledgePackageImportedEvent
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.service.PackageValidationEngine

class ImportKnowledgePackageUseCase(
    private val knowledgeRepository: KnowledgeRepository,
    private val validationEngine: PackageValidationEngine
) {
    suspend fun execute(
        pkg: KnowledgePackage,
        currentTimeMs: Long
    ): KnowledgePackageImportedEvent {
        require(validationEngine.validatePackage(pkg)) { "Package structural validation failed for ID ${pkg.id.value}" }

        knowledgeRepository.importPackage(pkg)

        return KnowledgePackageImportedEvent(
            eventId = "evt_pkg_imp_${pkg.id.value}_$currentTimeMs",
            occurredAt = currentTimeMs,
            aggregateId = pkg.id.value,
            version = pkg.version,
            totalCardsImported = pkg.cards.size,
            totalTopicsImported = pkg.topics.size
        )
    }
}
