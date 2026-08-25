package com.learnwitme.core.storage.repository

import com.learnwitme.core.application.ai.feedback.AIInteractionType
import com.learnwitme.core.application.ai.feedback.AILearningFeedback
import com.learnwitme.core.application.ai.feedback.AILearningInteraction
import com.learnwitme.core.application.ai.feedback.AILearningOutcome
import com.learnwitme.core.application.ai.feedback.AIOutcomeStatus
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.storage.local.dao.AILearningInteractionDao
import com.learnwitme.core.storage.mapper.AILearningInteractionMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AILearningInteractionRepositoryImplTest {
    private val dao = mockk<AILearningInteractionDao>(relaxed = true)
    private val repository = AILearningInteractionRepositoryImpl(dao)

    @Test
    fun repositoryDelegatesPersistenceAndRetrievalThroughTheMapper() = runBlocking {
        val interaction = AILearningInteraction(
            interactionId = "interaction-1",
            userId = UserId("user-1"),
            action = AIInteractionType.QUIZ,
            startedAt = 1_000L
        )
        val feedback = AILearningFeedback("interaction-1", 4, createdAt = 2_000L)
        val outcome = AILearningOutcome("interaction-1", AIOutcomeStatus.COMPLETED, completedAt = 2_100L)

        coEvery { dao.getInteraction("interaction-1") } returns AILearningInteractionMapper.toEntity(interaction)
        coEvery { dao.getFeedback("interaction-1") } returns AILearningInteractionMapper.toEntity(feedback)
        coEvery { dao.getOutcome("interaction-1") } returns AILearningInteractionMapper.toEntity(outcome)

        repository.saveInteraction(interaction)
        repository.saveFeedback(feedback)
        repository.saveOutcome(outcome)

        assertEquals(interaction, repository.getInteraction("interaction-1"))
        assertEquals(feedback, repository.getFeedback("interaction-1"))
        assertEquals(outcome, repository.getOutcome("interaction-1"))
        coVerify { dao.insertInteraction(AILearningInteractionMapper.toEntity(interaction)) }
        coVerify { dao.insertFeedback(AILearningInteractionMapper.toEntity(feedback)) }
        coVerify { dao.insertOutcome(AILearningInteractionMapper.toEntity(outcome)) }
    }

    @Test
    fun repositoryDelegatesSaveArtifactsToDaoInsertArtifacts() = runBlocking {
        val interaction = AILearningInteraction(
            interactionId = "interaction-2",
            userId = UserId("user-1"),
            action = AIInteractionType.LESSON,
            startedAt = 1_000L
        )
        val outcome = AILearningOutcome("interaction-2", AIOutcomeStatus.COMPLETED, completedAt = 2_100L)

        repository.saveArtifacts(
            interaction = interaction,
            outcome = outcome,
            feedback = null,
            impact = null
        )

        coVerify {
            dao.insertArtifacts(
                interaction = AILearningInteractionMapper.toEntity(interaction),
                outcome = AILearningInteractionMapper.toEntity(outcome),
                feedback = null,
                impact = null
            )
        }
    }
}
