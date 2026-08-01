package com.learnwitme.core.domain.ai.orchestrator

import kotlinx.serialization.Serializable

/**
 * Immutable snapshot of the current state of an AI execution.
 */
@Serializable
data class AIExecutionState(
    val plan: AIExecutionPlan,
    val status: AIExecutionStatus = AIExecutionStatus.CREATED,
    val startedAt: Long = 0L,
    val finishedAt: Long? = null,
    val currentAttempt: Int = 0,
    val lastFailureReason: String? = null
) {
    val isTerminal: Boolean
        get() = status == AIExecutionStatus.SUCCESS ||
                status == AIExecutionStatus.FAILED ||
                status == AIExecutionStatus.TIMEOUT ||
                status == AIExecutionStatus.CANCELLED
}
