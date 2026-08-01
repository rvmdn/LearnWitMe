package com.learnwitme.core.domain.ai.orchestrator

import kotlinx.serialization.Serializable

/**
 * Execution status lifecycle stages for AI Runtime orchestration.
 */
@Serializable
enum class AIExecutionStatus {
    CREATED,
    VALIDATING,
    READY,
    EXECUTING,
    SUCCESS,
    FAILED,
    TIMEOUT,
    CANCELLED
}
