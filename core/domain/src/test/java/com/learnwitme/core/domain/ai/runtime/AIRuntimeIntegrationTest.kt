package com.learnwitme.core.domain.ai.runtime

import com.learnwitme.core.domain.ai.orchestrator.AIExecutionStatus
import com.learnwitme.core.domain.ai.orchestrator.AIOrchestrator
import com.learnwitme.core.domain.ai.prompt.PromptBuildRequest
import com.learnwitme.core.domain.ai.prompt.PromptPipeline
import com.learnwitme.core.domain.ai.prompt.PromptTemplate
import com.learnwitme.core.domain.ai.prompt.PromptVariables
import com.learnwitme.core.domain.ai.response.ResponsePipeline
import com.learnwitme.core.domain.ai.session.AISessionManager
import com.learnwitme.core.domain.ai.session.ConversationMessage
import com.learnwitme.core.domain.ai.session.ConversationRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end integration and architecture verification tests for Sprint 10 AI Runtime layers:
 * Runtime Contracts -> Prompt Pipeline -> Session Runtime -> AI Orchestrator -> Response Pipeline
 */
class AIRuntimeIntegrationTest {

    private val promptPipeline = PromptPipeline()
    private val sessionManager = AISessionManager()
    private val orchestrator = AIOrchestrator()
    private val responsePipeline = ResponsePipeline()

    @Test
    fun testSuccessfulExecutionFlow() {
        // 1. Prepare Request & Prompt Pipeline
        val template = PromptTemplate(
            id = "tpl_01",
            name = "QuizGenerator",
            systemTemplate = "You are a teacher for {{subject}}.",
            userTemplate = "Generate quiz for topic: {{topic}}"
        )
        val variables = PromptVariables.from(mapOf("subject" to "Kotlin", "topic" to "Coroutines"))
        val buildRequest = PromptBuildRequest(template = template, variables = variables)

        val promptResult = promptPipeline.execute(buildRequest)
        assertTrue(promptResult.isValid)
        assertEquals("You are a teacher for Kotlin.", promptResult.renderedSystemPrompt)
        assertEquals("Generate quiz for topic: Coroutines", promptResult.renderedUserPrompt)

        // 2. Prepare Session Runtime
        val session = sessionManager.startSession("sess_01", "conv_01", "user_100", 1000L)
        val userMsg = ConversationMessage("msg_01", ConversationRole.USER, promptResult.renderedUserPrompt, createdAt = 1001L)
        val sessionWithMsg = sessionManager.appendMessage(session, userMsg, 1002L)
        val sessionContext = sessionManager.rebuildContext(sessionWithMsg, promptResult, maxTokens = 2048)
        assertFalse(sessionContext.currentContextWindow.messages.isEmpty())

        // 3. Orchestration
        val execRequest = AIExecutionRequest(
            requestId = "req_01",
            userId = "user_100",
            prompt = promptResult.renderedUserPrompt,
            systemPrompt = promptResult.renderedSystemPrompt
        )
        val plan = orchestrator.createExecutionPlan("exec_01", execRequest, correlationId = "corr_01")
        val validation = orchestrator.validate(plan, isProviderAvailable = true)
        assertTrue(validation.isValid)

        val initialState = orchestrator.prepareExecution(plan, 1000L)
        val executingState = orchestrator.markExecuting(initialState, attempt = 1, timestamp = 1010L)
        assertEquals(AIExecutionStatus.EXECUTING, executingState.status)

        val rawProviderResponse = AIExecutionResponse(
            requestId = "req_01",
            provider = "GEMINI",
            model = "gemini-1.5-pro",
            content = "## Quiz\n* What is a Job?\n* What is a Deferred?",
            executionTimeMs = 250L
        )

        val (finalState, audit) = orchestrator.markCompleted(executingState, rawProviderResponse, 1250L)
        assertEquals(AIExecutionStatus.SUCCESS, finalState.status)
        assertEquals(250L, audit.executionDurationMs)

        // 4. Response Pipeline
        val processedResponse = responsePipeline.process(rawProviderResponse.content, processingTimeMs = audit.executionDurationMs)
        assertTrue(processedResponse.validationResult.isValid)
        assertTrue(processedResponse.metadata.containsMarkdown)
        assertEquals("## Quiz\n- What is a Job?\n- What is a Deferred?", processedResponse.cleanContent)
    }

    @Test
    fun testValidationFailure() {
        val request = AIExecutionRequest(
            requestId = "req_val_fail",
            prompt = "Sample prompt"
        )
        val plan = orchestrator.createExecutionPlan("exec_invalid", request)
        val validation = orchestrator.validate(plan, isProviderAvailable = false)

        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("unavailable") })
    }

    @Test
    fun testTimeout() {
        val request = AIExecutionRequest(requestId = "req_timeout", prompt = "Analyze code")
        val plan = orchestrator.createExecutionPlan("exec_timeout", request)
        val readyState = orchestrator.prepareExecution(plan, 2000L)
        val executingState = orchestrator.markExecuting(readyState, 1, 2000L)

        val (timedOutState, audit) = orchestrator.markTimeout(executingState, 32000L)

        assertEquals(AIExecutionStatus.TIMEOUT, timedOutState.status)
        assertEquals(AIExecutionStatus.TIMEOUT, audit.finalStatus)
        assertEquals(30000L, audit.executionDurationMs)
    }

    @Test
    fun testCancelledExecution() {
        val request = AIExecutionRequest(requestId = "req_cancel", prompt = "Long operation")
        val plan = orchestrator.createExecutionPlan("exec_cancel", request)
        val readyState = orchestrator.prepareExecution(plan, 3000L)
        val executingState = orchestrator.markExecuting(readyState, 1, 3000L)

        val (cancelledState, audit) = orchestrator.cancel(executingState, 3500L)

        assertEquals(AIExecutionStatus.CANCELLED, cancelledState.status)
        assertEquals(AIExecutionStatus.CANCELLED, audit.finalStatus)
        assertEquals(500L, audit.executionDurationMs)
    }

    @Test
    fun testResponseWithMarkdown() {
        val rawMarkdown = "#### Heading 4\n* First bullet\n+ Second bullet"
        val result = responsePipeline.process(rawMarkdown)

        assertTrue(result.metadata.containsMarkdown)
        assertEquals("#### Heading 4\n- First bullet\n- Second bullet", result.cleanContent)
    }

    @Test
    fun testResponseWithJsonBlock() {
        val rawJsonMarkdown = "Here is the result:\n```json\n{\n  \"score\": 100,\n  \"passed\": true\n}\n```"
        val result = responsePipeline.process(rawJsonMarkdown)

        assertTrue(result.metadata.containsJson)
        assertNotNull(result.extractedJson)
        assertTrue(result.extractedJson!!.contains("\"score\": 100"))
    }

    @Test
    fun testPromptExceedingBudget() {
        val longText = "word ".repeat(3000)
        val template = PromptTemplate(id = "tpl_big", name = "Big", userTemplate = longText)
        val policy = AIExecutionPolicy(timeoutMs = 1000L) // limit max tokens to 1000
        val request = PromptBuildRequest(template = template, executionPolicy = policy)

        val result = promptPipeline.execute(request)

        assertTrue(result.estimatedTokenCount > 1000)
        assertTrue(result.warnings.any { it.contains("exceeds max token limit") })
    }

    @Test
    fun testEmptyResponse() {
        val emptyContent = "   "
        val result = responsePipeline.process(emptyContent)

        assertFalse(result.validationResult.isValid)
        assertTrue(result.validationResult.errors.contains("Response content is empty or blank"))
    }

    @Test
    fun testMalformedMarkdown() {
        val malformedCodeFence = "Some explanation:\n```kotlin\nval x = 10\n"
        val result = responsePipeline.process(malformedCodeFence)

        assertTrue(result.warnings.any { it.contains("unclosed markdown code block") })
    }

    @Test
    fun testDeterministicRepeatedExecution() {
        val template = PromptTemplate(
            id = "tpl_det",
            name = "Deterministic",
            systemTemplate = "System {{sysKey}}",
            userTemplate = "User {{userKey}}"
        )
        val variables = PromptVariables.from(mapOf("sysKey" to "v1", "userKey" to "v2"))
        val request = PromptBuildRequest(template = template, variables = variables)

        val firstRun = promptPipeline.execute(request)
        for (i in 1..10) {
            val repeatedRun = promptPipeline.execute(request)
            assertEquals(firstRun.renderedSystemPrompt, repeatedRun.renderedSystemPrompt)
            assertEquals(firstRun.renderedUserPrompt, repeatedRun.renderedUserPrompt)
            assertEquals(firstRun.estimatedTokenCount, repeatedRun.estimatedTokenCount)
            assertEquals(firstRun.isValid, repeatedRun.isValid)
        }
    }
}
