package com.learnwitme.core.infrastructure.ai.openai

import com.learnwitme.core.domain.ai.AIProviderException
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAIProviderTest {

    private val provider = OpenAIProvider(
        openAIConfig = OpenAIConfiguration(apiKey = "dummy_openai_key")
    )

    @Test
    fun testGenerateExplanation() = runBlocking {
        val request = AIRequest(prompt = "Clean Architecture principles")
        val response = provider.generateExplanation(request)

        assertNotNull(response)
        assertEquals("gpt-4o-mini", response.modelId)
        assertTrue(response.content.contains("Clean Architecture"))
        assertEquals(60, response.tokensUsed)
    }

    @Test
    fun testGenerateLesson() = runBlocking {
        val request = AIRequest(prompt = "Kotlin StateFlow")
        val response = provider.generateLesson(request)

        assertNotNull(response)
        assertEquals("gpt-4o-mini", response.modelId)
        assertTrue(response.content.contains("Kotlin StateFlow"))
    }

    @Test
    fun testGenerateQuiz() = runBlocking {
        val request = AIRequest(prompt = "Android ViewModel lifecycle")
        val response = provider.generateQuiz(request)

        assertNotNull(response)
        assertEquals("gpt-4o-mini", response.modelId)
        assertTrue(response.content.contains("Android ViewModel"))
    }

    @Test
    fun testSimplifyContent() = runBlocking {
        val request = AIRequest(prompt = "Asynchronous programming with Coroutines")
        val response = provider.simplifyContent(request)

        assertNotNull(response)
        assertEquals("gpt-4o-mini", response.modelId)
        assertTrue(response.content.contains("Coroutines"))
    }

    @Test
    fun testExecuteRuntimeRequest() {
        val runtimeRequest = AIExecutionRequest(
            requestId = "req_openai_01",
            prompt = "OpenAI runtime test prompt"
        )
        val response = provider.executeRuntimeRequest(runtimeRequest)

        assertNotNull(response)
        assertEquals("req_openai_01", response.requestId)
        assertEquals("OPENAI", response.provider)
        assertEquals("gpt-4o-mini", response.model)
        assertEquals(60, response.tokenUsage.totalTokens)
    }

    @Test(expected = AIProviderException.InvalidApiKeyException::class)
    fun testMissingApiKeyThrowsDomainException() {
        val providerNoKey = OpenAIProvider(
            openAIConfig = OpenAIConfiguration(apiKey = "")
        )
        val runtimeRequest = AIExecutionRequest(
            requestId = "req_err",
            prompt = "Will fail due to missing API key"
        )
        providerNoKey.executeRuntimeRequest(runtimeRequest)
    }
}
