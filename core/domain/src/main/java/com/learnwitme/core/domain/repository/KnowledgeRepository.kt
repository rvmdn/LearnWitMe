package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Skill

/**
 * Responsibility: Bounded Context repository contract managing static Knowledge assets
 * (KnowledgeCards and KnowledgePackages) using domain-driven business language.
 */
interface KnowledgeRepository {
    // --- Card Retrieval & Authoring ---
    suspend fun findCard(id: KnowledgeCardId): KnowledgeCard?
    suspend fun getCard(id: KnowledgeCardId): KnowledgeCard? = findCard(id)
    suspend fun saveCard(card: KnowledgeCard)
    suspend fun deleteCard(id: KnowledgeCardId)

    // --- Business Query Methods ---
    suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard>
    suspend fun searchBySkills(skills: List<Skill>): List<KnowledgeCard>

    // --- Package Operations ---
    suspend fun findPackage(packageId: KnowledgePackageId): KnowledgePackage?
    suspend fun importPackage(pkg: KnowledgePackage)
    suspend fun publishPackage(packageId: KnowledgePackageId)
}
