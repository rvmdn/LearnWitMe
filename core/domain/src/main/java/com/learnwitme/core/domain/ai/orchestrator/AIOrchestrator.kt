package com.learnwitme.core.domain.ai.orchestrator

import com.learnwitme.core.domain.ai.runtime.AIExecutionContext
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionResponse

/**
 * Pure state transition coordinator for AI execution lifecycle orchestration.
 * Does NOT invoke external providers, networks, or SDKs.
 */
class AIOrchestrator(
    private val planner: AIExecutionPlanner = AIExecutionPlanner(),
    private val validator: AIExecutionValidator = AIExecutionValidator()
) {

    fun createExecutionPlan(
        executionId: String,
        request: AIExecutionRequest,
        context: AIExecutionContext = AIExecutionContext.DEFAULT,
        policy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT,
        retryPolicy: AIRetryPolicy = AIRetryPolicy.DEFAULT,
        correlationId: String = ""
    ): AIExecutionPlan {
        return planner.createPlan(
            executionId = executionId,
            request = request,
            context = context,
            policy = policy,
            retryPolicy = retryPolicy,
            correlationId = correlationId
        )
    }

    fun validate(
        plan: AIExecutionPlan,
        isProviderAvailable: Boolean = true
    ): AIExecutionValidator.ValidationResult {
        return validator.validate(
            request = plan.executionRequest,
            context = plan.executionContext,
            policy = plan.executionPolicy,
            isProviderAvailable = isProviderAvailable
        )
    }

    fun prepareExecution(
        plan: AIExecutionPlan,
        timestamp: Long = 0L
    ): AIExecutionState {
        return AIExecutionState(
            plan = plan,
            status = AIExecutionStatus.READY,
            startedAt = timestamp,
            currentAttempt = 0
        )
    }

    fun markExecuting(
        state: AIExecutionState,
        attempt: Int = state.currentAttempt + 1,
        timestamp: Long = 0L
    ): AIExecutionState {
        return state.copy(
            status = AIExecutionStatus.EXECUTING,
            currentAttempt = attempt,
            startedAt = if (state.startedAt == 0L) timestamp else state.startedAt
        )
    }

    fun markCompleted(
        state: AIExecutionState,
        response: AIExecutionResponse,
        timestamp: Long = 0L
    ): Pair<AIExecutionState, AIExecutionAudit> {
        val newState = state.copy(
            status = AIExecutionStatus.SUCCESS,
            finishedAt = timestamp
        )
        val duration = (timestamp - state.startedAt).coerceAtLeast(0L)
        val audit = AIExecutionAudit(
            executionId = state.plan.executionId,
            correlationId = state.plan.correlationId,
            provider = response.provider,
            model = response.model,
            attempts = state.currentAttempt,
            executionDurationMs = duration,
            finalStatus = AIExecutionStatus.SUCCESS,
            metadata = response.metadata
        )
        return Pair(newState, audit)
    }

    fun markFailure(
        state: AIExecutionState,
        failureReason: String,
        timestamp: Long = 0L
    ): Pair<AIExecutionState, AIExecutionAudit> {
        val newState = state.copy(
            status = AIExecutionStatus.FAILED,
            finishedAt = timestamp,
            lastFailureReason = failureReason
        )
        val duration = (timestamp - state.startedAt).coerceAtLeast(0L)
        val audit = AIExecutionAudit(
            executionId = state.plan.executionId,
            correlationId = state.plan.correlationId,
            provider = state.plan.preferredProvider,
            attempts = state.currentAttempt,
            executionDurationMs = duration,
            finalStatus = AIExecutionStatus.FAILED,
            metadata = mapOf("failureReason" to failureReason)
        )
        return Pair(newState, audit)
    }

    fun markTimeout(
        state: AIExecutionState,
        timestamp: Long = 0L
    ): Pair<AIExecutionState, AIExecutionAudit> {
        val newState = state.copy(
            status = AIExecutionStatus.TIMEOUT,
            finishedAt = timestamp,
            lastFailureReason = "Execution timed out after ${state.plan.timeoutMs} ms"
        )
        val duration = (timestamp - state.startedAt).coerceAtLeast(0L)
        val audit = AIExecutionAudit(
            executionId = state.plan.executionId,
            correlationId = state.plan.correlationId,
            provider = state.plan.preferredProvider,
            attempts = state.currentAttempt,
            executionDurationMs = duration,
            finalStatus = AIExecutionStatus.TIMEOUT,
            metadata = mapOf("timeoutMs" to state.plan.timeoutMs.toString())
        )
        return Pair(newState, audit)
    }

    fun cancel(
        state: AIExecutionState,
        timestamp: Long = 0L
    ): Pair<AIExecutionState, AIExecutionAudit> {
        val newState = state.copy(
            status = AIExecutionStatus.CANCELLED,
            finishedAt = timestamp
        )
        val duration = (timestamp - state.startedAt).coerceAtLeast(0L)
        val audit = AIExecutionAudit(
            executionId = state.plan.executionId,
            correlationId = state.plan.correlationId,
            provider = state.plan.preferredProvider,
            attempts = state.currentAttempt,
            executionDurationMs = duration,
            finalStatus = AIExecutionStatus.CANCELLED
        )
        return Pair(newState, audit)
    }
}
