package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.repository.KnowledgeRepository

class PublishKnowledgePackageUseCase(
    private val knowledgeRepository: KnowledgeRepository
) {
    suspend fun execute(packageId: KnowledgePackageId): KnowledgePackage {
        val existingPkg = knowledgeRepository.findPackage(packageId)
            ?: throw IllegalStateException("Package not found for ID: ${packageId.value}")

        val publishedPkg = existingPkg.publish()
        knowledgeRepository.importPackage(publishedPkg)
        knowledgeRepository.publishPackage(packageId)
        return publishedPkg
    }
}
