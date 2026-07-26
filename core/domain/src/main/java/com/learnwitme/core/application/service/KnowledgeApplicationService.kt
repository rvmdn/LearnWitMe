package com.learnwitme.core.application.service

import com.learnwitme.core.application.dispatcher.DispatcherProvider
import com.learnwitme.core.application.result.AppResult
import com.learnwitme.core.domain.event.KnowledgePackageImportedEvent
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.KnowledgePackage
import com.learnwitme.core.domain.model.KnowledgePackageId
import com.learnwitme.core.domain.model.Skill
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.usecase.FindKnowledgeCardUseCase
import com.learnwitme.core.domain.usecase.ImportKnowledgePackageUseCase
import com.learnwitme.core.domain.usecase.KnowledgeCardDetails
import com.learnwitme.core.domain.usecase.PublishKnowledgePackageUseCase
import kotlinx.coroutines.withContext

/**
 * Responsibility: Application service orchestrating static Knowledge content workflows
 * (importing packages, publishing bundles, searching cards by skills, and retrieving card details).
 */
class KnowledgeApplicationService(
    private val importKnowledgePackageUseCase: ImportKnowledgePackageUseCase,
    private val publishKnowledgePackageUseCase: PublishKnowledgePackageUseCase,
    private val findKnowledgeCardUseCase: FindKnowledgeCardUseCase,
    private val knowledgeRepository: KnowledgeRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun importPackage(pkg: KnowledgePackage, currentTimeMs: Long): AppResult<KnowledgePackageImportedEvent> =
        withContext(dispatcherProvider.io) {
            try {
                val event = importKnowledgePackageUseCase.execute(pkg, currentTimeMs)
                AppResult.Success(event)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }

    suspend fun publishPackage(packageId: KnowledgePackageId): AppResult<KnowledgePackage> =
        withContext(dispatcherProvider.io) {
            try {
                val publishedPkg = publishKnowledgePackageUseCase.execute(packageId)
                AppResult.Success(publishedPkg)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }

    suspend fun findCardDetails(userId: UserId, cardId: KnowledgeCardId): AppResult<KnowledgeCardDetails?> =
        withContext(dispatcherProvider.io) {
            try {
                val details = findKnowledgeCardUseCase.execute(userId, cardId)
                AppResult.Success(details)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }

    suspend fun searchCardsBySkills(skills: List<Skill>): AppResult<List<KnowledgeCard>> =
        withContext(dispatcherProvider.io) {
            try {
                val cards = knowledgeRepository.searchBySkills(skills)
                AppResult.Success(cards)
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }
}
