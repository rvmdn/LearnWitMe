package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.domain.model.LearningMemory

/**
 * Rejection reasons when integrating feedback with Learning Memory.
 */
enum class AILearningMemoryIntegrationRejectionReason {
    FEEDBACK_VALIDATION_FAILED,
    USER_ID_MISMATCH
}

/**
 * Immutable result of integrating AI learning feedback into a Learning Memory projection.
 */
sealed interface AILearningMemoryIntegrationResult {
    data class Success(
        val interactionId: String,
        val impact: AILearningMemoryImpact,
        val updatedMemory: LearningMemory? = null,
        val message: String = "Successfully computed Learning Memory impact"
    ) : AILearningMemoryIntegrationResult

    data class Rejected(
        val interactionId: String,
        val reason: AILearningMemoryIntegrationRejectionReason,
        val message: String
    ) : AILearningMemoryIntegrationResult

    val isSuccess: Boolean get() = this is Success
    val isRejected: Boolean get() = this is Rejected
}
