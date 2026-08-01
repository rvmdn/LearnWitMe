package com.learnwitme.core.infrastructure.ai.gemini

import com.learnwitme.core.domain.ai.AIProviderException
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiProviderTest {

    private val provider = GeminiProvider(
        geminiConfig = GeminiConfiguration(apiKey = "dummy_api_key")
    )

    @Test
    fun testGenerateExplanation() = runBlocking {
        val request = AIRequest(prompt = "Dependency Injection")
        val response = provider.generateExplanation(request)

        assertNotNull(response)
        assertEquals("gemini-1.5-flash", response.modelId)
        assertTrue(response.content.contains("Dependency Injection"))
        assertEquals(50, response.tokensUsed)
    }

    @Test
    fun testGenerateLesson() = runBlocking {
        val request = AIRequest(prompt = "Kotlin Coroutines")
        val response = provider.generateLesson(request)

        assertNotNull(response)
        assertEquals("gemini-1.5-flash", response.modelId)
        assertTrue(response.content.contains("Kotlin Coroutines"))
    }

    @Test
    fun testGenerateQuiz() = runBlocking {
        val request = AIRequest(prompt = "Flow operators")
        val response = provider.generateQuiz(request)

        assertNotNull(response)
        assertEquals("gemini-1.5-flash", response.modelId)
        assertTrue(response.content.contains("Flow operators"))
    }

    @Test
    fun testSimplifyContent() = runBlocking {
        val request = AIRequest(prompt = "Monads in functional programming")
        val response = provider.simplifyContent(request)

        assertNotNull(response)
        assertEquals("gemini-1.5-flash", response.modelId)
        assertTrue(response.content.contains("Monads"))
    }

    @Test
    fun testExecuteRuntimeRequest() {
        val runtimeRequest = AIExecutionRequest(
            requestId = "req_rt_01",
            prompt = "Runtime contract test prompt"
        )
        val response = provider.executeRuntimeRequest(runtimeRequest)

        assertNotNull(response)
        assertEquals("req_rt_01", response.requestId)
        assertEquals("GEMINI", response.provider)
        assertEquals("gemini-1.5-flash", response.model)
        assertEquals(50, response.tokenUsage.totalTokens)
    }

    @Test(expected = AIProviderException.InvalidApiKeyException::class)
    fun testMissingApiKeyThrowsDomainException() {
        val providerNoKey = GeminiProvider(
            geminiConfig = GeminiConfiguration(apiKey = "")
        )
        val runtimeRequest = AIExecutionRequest(
            requestId = "req_err",
            prompt = "Will fail due to missing API key"
        )
        providerNoKey.executeRuntimeRequest(runtimeRequest)
    }
}
