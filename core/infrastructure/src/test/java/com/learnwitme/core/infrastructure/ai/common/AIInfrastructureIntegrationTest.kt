package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.domain.ai.AIProviderException
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.infrastructure.ai.AIProviderConfiguration
import com.learnwitme.core.infrastructure.ai.AITransportRequestDto
import com.learnwitme.core.infrastructure.ai.ProviderExceptionMapper
import com.learnwitme.core.infrastructure.ai.gemini.GeminiConfiguration
import com.learnwitme.core.infrastructure.ai.gemini.GeminiProvider
import com.learnwitme.core.infrastructure.ai.ollama.OllamaConfiguration
import com.learnwitme.core.infrastructure.ai.ollama.OllamaProvider
import com.learnwitme.core.infrastructure.ai.openai.OpenAIConfiguration
import com.learnwitme.core.infrastructure.ai.openai.OpenAIProvider
import com.learnwitme.core.infrastructure.ai.openrouter.OpenRouterConfiguration
import com.learnwitme.core.infrastructure.ai.openrouter.OpenRouterProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AIInfrastructureIntegrationTest {

    private val selector = ProviderSelector()
    private val resolver = ProviderCapabilityResolver()
    private val fallbackChain = ProviderFallbackChain(resolver)
    private val exceptionMapper = ProviderExceptionMapper()
    private val retryExecutor = RetryExecutor()

    private val geminiMeta = ProviderMetadata("GEMINI", "GEMINI", GeminiConfiguration.DEFAULT_MODEL)
    private val openAIMeta = ProviderMetadata("OPENAI", "OPENAI", OpenAIConfiguration.DEFAULT_MODEL)
    private val openRouterMeta = ProviderMetadata("OPENROUTER", "OPENROUTER", OpenRouterConfiguration.DEFAULT_MODEL)
    private val ollamaMeta = ProviderMetadata("OLLAMA", "OLLAMA", OllamaConfiguration.DEFAULT_MODEL, ProviderCapability(supportsOffline = true))

    private val providerMap = mapOf(
        "GEMINI" to geminiMeta,
        "OPENAI" to openAIMeta,
        "OPENROUTER" to openRouterMeta,
        "OLLAMA" to ollamaMeta
    )

    @Test
    fun testProviderSelectionAutoMode() {
        val selected = selector.selectProviderName(providerMap, ProviderPriorityPolicy(selectionMode = ProviderSelectionMode.AUTO))
        assertEquals("GEMINI", selected)
    }

    @Test
    fun testExplicitProviderMode() {
        val selected = selector.selectProviderName(providerMap, ProviderPriorityPolicy(selectionMode = ProviderSelectionMode.OPENAI))
        assertEquals("OPENAI", selected)
    }

    @Test
    fun testOfflinePreferredSelection() {
        val offlinePolicy = DefaultProviderPolicy.OFFLINE_PREFERRED
        val requiredCap = ProviderCapability(supportsOffline = true)
        val selected = selector.selectProviderName(providerMap, offlinePolicy, requiredCap)
        assertEquals("OLLAMA", selected)
    }

    @Test
    fun testRetryFlowOnRateLimit() {
        var attempts = 0
        val result = retryExecutor.executeWithRetry(maxRetries = 3) { attempt ->
            attempts = attempt
            if (attempt < 3) {
                throw AIProviderException.RateLimitException("Simulated 429 Rate Limit")
            }
            "Success on attempt $attempt"
        }
        assertEquals(3, attempts)
        assertEquals("Success on attempt 3", result)
    }

    @Test
    fun testTimeoutFlow() {
        try {
            retryExecutor.executeWithRetry(maxRetries = 2) {
                throw AIProviderException.NetworkTimeoutException("Simulated 504 Gateway Timeout")
            }
            fail("Expected NetworkTimeoutException")
        } catch (e: AIProviderException.NetworkTimeoutException) {
            assertTrue(e.message!!.contains("Simulated 504"))
        }
    }

    @Test
    fun testFallbackChainExecution() {
        val geminiProvider = GeminiProvider(GeminiConfiguration(apiKey = "gemini_key"))
        val openAIProvider = OpenAIProvider(OpenAIConfiguration(apiKey = "openai_key"))

        val providers = listOf(
            geminiMeta to {
                throw exceptionMapper.mapToDomainException(429, "Rate limit exceeded", "GEMINI")
            },
            openAIMeta to {
                openAIProvider.executeRuntimeRequest(AIExecutionRequest("req_fb", prompt = "Fallback prompt"))
            }
        )

        val response = fallbackChain.executeWithFallback(providers)
        assertNotNull(response)
        assertEquals("OPENAI", response.provider)
    }

    @Test
    fun testExceptionMappingForHttpStatusCodes() {
        val authError = exceptionMapper.mapToDomainException(401, "Unauthorized", "GEMINI")
        assertTrue(authError is AIProviderException.InvalidApiKeyException)

        val notFoundError = exceptionMapper.mapToDomainException(404, "Model not found", "OPENAI")
        assertTrue(notFoundError is AIProviderException.ModelNotFoundException)

        val rateLimitError = exceptionMapper.mapToDomainException(429, "Too many requests", "OPENROUTER")
        assertTrue(rateLimitError is AIProviderException.RateLimitException)

        val timeoutError = exceptionMapper.mapToDomainException(504, "Gateway Timeout", "OLLAMA")
        assertTrue(timeoutError is AIProviderException.NetworkTimeoutException)
    }

    @Test
    fun testSharedTransportReusabilityAcrossAllProviders() {
        val request = AIExecutionRequest("req_shared", prompt = "Shared transport test")

        val geminiResp = GeminiProvider(GeminiConfiguration(apiKey = "key")).executeRuntimeRequest(request)
        assertEquals("GEMINI", geminiResp.provider)

        val openAIResp = OpenAIProvider(OpenAIConfiguration(apiKey = "key")).executeRuntimeRequest(request)
        assertEquals("OPENAI", openAIResp.provider)

        val openRouterResp = OpenRouterProvider(OpenRouterConfiguration(apiKey = "key")).executeRuntimeRequest(request)
        assertEquals("OPENROUTER", openRouterResp.provider)

        val ollamaResp = OllamaProvider(OllamaConfiguration()).executeRuntimeRequest(request)
        assertEquals("OLLAMA", ollamaResp.provider)
    }

    @Test
    fun testResponseMappingToDomainResponse() {
        val provider = GeminiProvider(GeminiConfiguration(apiKey = "key"))
        val response = provider.executeRuntimeRequest(AIExecutionRequest("req_map", prompt = "Testing response mapping"))

        assertEquals("req_map", response.requestId)
        assertNotNull(response.content)
        assertEquals("STOP", response.finishReason)
        assertTrue(response.tokenUsage.totalTokens > 0)
    }
}
