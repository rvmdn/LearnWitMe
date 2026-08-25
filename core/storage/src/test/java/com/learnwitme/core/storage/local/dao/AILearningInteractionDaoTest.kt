package com.learnwitme.core.storage.local.dao

import androidx.room.Room
import com.learnwitme.core.storage.local.LearnWitMeDatabase
import com.learnwitme.core.storage.local.entity.AILearningFeedbackEntity
import com.learnwitme.core.storage.local.entity.AILearningInteractionEntity
import com.learnwitme.core.storage.local.entity.AILearningMemoryImpactEntity
import com.learnwitme.core.storage.local.entity.AILearningOutcomeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AILearningInteractionDaoTest {
    private lateinit var database: LearnWitMeDatabase
    private lateinit var dao: AILearningInteractionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            LearnWitMeDatabase::class.java
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
        dao = database.aiLearningInteractionDao()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) {
            database.close()
        }
    }

    @Test
    fun interactionArtifactsUseSharedInteractionIdAndPreserveTimestampsAndNullables() = runBlocking {
        val initial = interaction(completionTokens = 10, completedAt = null)
        dao.insertInteraction(initial)
        dao.insertInteraction(initial.copy(completionTokens = 20, completedAt = 2_000L))
        dao.insertFeedback(AILearningFeedbackEntity("interaction-1", 5, null, null, 2_100L))
        dao.insertOutcome(AILearningOutcomeEntity("interaction-1", "COMPLETED", null, 2_000L, "{}"))
        dao.insertMemoryImpact(
            AILearningMemoryImpactEntity(
                interactionId = "interaction-1",
                cardId = null,
                topicCode = null,
                topicName = null,
                topicDescription = null,
                action = "LESSON",
                outcomeStatus = null,
                usefulnessRating = 5,
                confidenceSignal = null,
                classification = "POSITIVE_REINFORCEMENT",
                confidenceDelta = 0.2,
                calculatedAt = 2_200L
            )
        )

        val persistedInteraction = requireNotNull(dao.getInteraction("interaction-1"))
        assertEquals(20, persistedInteraction.completionTokens)
        assertEquals(2_000L, persistedInteraction.completedAt)
        assertEquals(2_100L, requireNotNull(dao.getFeedback("interaction-1")).createdAt)
        assertNull(requireNotNull(dao.getFeedback("interaction-1")).comment)
        assertEquals(2_000L, requireNotNull(dao.getOutcome("interaction-1")).completedAt)
        assertNull(requireNotNull(dao.getOutcome("interaction-1")).summary)
        assertEquals(2_200L, requireNotNull(dao.getMemoryImpact("interaction-1")).calculatedAt)
        assertNull(dao.getFeedback("missing"))
    }

    private fun interaction(completionTokens: Int, completedAt: Long?) = AILearningInteractionEntity(
        interactionId = "interaction-1",
        userId = "user-1",
        action = "LESSON",
        cardId = null,
        topicCode = null,
        topicName = null,
        topicDescription = null,
        startedAt = 1_000L,
        completedAt = completedAt,
        provider = null,
        model = null,
        promptTokens = 10,
        completionTokens = completionTokens,
        personalizationPreset = null
    )
}
