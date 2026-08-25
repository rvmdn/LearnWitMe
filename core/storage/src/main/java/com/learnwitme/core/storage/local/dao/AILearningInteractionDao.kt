package com.learnwitme.core.storage.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.learnwitme.core.storage.local.entity.AILearningFeedbackEntity
import com.learnwitme.core.storage.local.entity.AILearningInteractionEntity
import com.learnwitme.core.storage.local.entity.AILearningMemoryImpactEntity
import com.learnwitme.core.storage.local.entity.AILearningOutcomeEntity

@Dao
interface AILearningInteractionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: AILearningInteractionEntity)

    @Query("SELECT * FROM ai_learning_interactions WHERE interactionId = :interactionId LIMIT 1")
    suspend fun getInteraction(interactionId: String): AILearningInteractionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: AILearningFeedbackEntity)

    @Query("SELECT * FROM ai_learning_feedback WHERE interactionId = :interactionId LIMIT 1")
    suspend fun getFeedback(interactionId: String): AILearningFeedbackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutcome(outcome: AILearningOutcomeEntity)

    @Query("SELECT * FROM ai_learning_outcomes WHERE interactionId = :interactionId LIMIT 1")
    suspend fun getOutcome(interactionId: String): AILearningOutcomeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryImpact(impact: AILearningMemoryImpactEntity)

    @Query("SELECT * FROM ai_learning_memory_impacts WHERE interactionId = :interactionId LIMIT 1")
    suspend fun getMemoryImpact(interactionId: String): AILearningMemoryImpactEntity?

    @Transaction
    suspend fun insertArtifacts(
        interaction: AILearningInteractionEntity,
        outcome: AILearningOutcomeEntity? = null,
        feedback: AILearningFeedbackEntity? = null,
        impact: AILearningMemoryImpactEntity? = null
    ) {
        insertInteraction(interaction)
        outcome?.let { insertOutcome(it) }
        feedback?.let { insertFeedback(it) }
        impact?.let { insertMemoryImpact(it) }
    }
}
