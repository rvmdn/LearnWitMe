package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId

/**
 * Responsibility: Repository contract for managing KnowledgePackage aggregate roots.
 */
interface KnowledgePackageRepository {
    suspend fun getPackage(id: KnowledgePackageId): KnowledgePackage?
    suspend fun savePackage(pkg: KnowledgePackage)
    suspend fun getActivePackages(): List<KnowledgePackage>
}
