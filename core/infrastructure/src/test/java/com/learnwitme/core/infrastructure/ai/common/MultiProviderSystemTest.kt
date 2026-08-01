package com.learnwitme.core.infrastructure.ai.common

import com.learnwitme.core.domain.ai.AIProviderException
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.ai.runtime.AIExecutionRequest
import com.learnwitme.core.infrastructure.ai.ollama.OllamaConfiguration
import com.learnwitme.core.infrastructure.ai.ollama.OllamaProvider
import com.learnwitme.core.infrastructure.ai.openrouter.OpenRouterConfiguration
import com.learnwitme.core.infrastructure.ai.openrouter.OpenRouterProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiProviderSystemTest {

    private val selector = ProviderSelector()
    private val resolver = ProviderCapabilityResolver()
    private val fallbackChain = ProviderFallbackChain(resolver)

    private val geminiMetadata = ProviderMetadata("GEMINI", "GEMINI", "gemini-1.5-flash")
    private val openAIMetadata = ProviderMetadata("OPENAI", "OPENAI", "gpt-4o-mini")
    private val openRouterMetadata = ProviderMetadata("OPENROUTER", "OPENROUTER", "anthropic/claude-3.5-sonnet")
    private val ollamaMetadata = ProviderMetadata("OLLAMA", "OLLAMA", "llama3", ProviderCapability(supportsOffline = true))

    private val registeredMetadata = mapOf(
        "GEMINI" to geminiMetadata,
        "OPENAI" to openAIMetadata,
        "OPENROUTER" to openRouterMetadata,
        "OLLAMA" to ollamaMetadata
    )

    @Test
    fun testAutoSelectionSelectsPreferredProvider() {
        val selected = selector.selectProviderName(registeredMetadata, DefaultProviderPolicy.DEFAULT)
        assertEquals("GEMINI", selected)
    }

    @Test
    fun testOfflineCapabilityFilterSelectsOllama() {
        val offlineReq = ProviderCapability(supportsOffline = true)
        val selected = selector.selectProviderName(registeredMetadata, DefaultProviderPolicy.DEFAULT, offlineReq)
        assertEquals("OLLAMA", selected)
    }

    @Test
    fun testFallbackChainNavigatesDownOrderOnFailure() {
        val providers = listOf(
            geminiMetadata to { throw AIProviderException.RateLimitException("Gemini 429") },
            openAIMetadata to { "Success from OpenAI" }
        )

        val result = fallbackChain.executeWithFallback(providers)
        assertEquals("Success from OpenAI", result)
    }

    @Test
    fun testOpenRouterProviderExecution() = runBlocking {
        val provider = OpenRouterProvider(config = OpenRouterConfiguration(apiKey = "dummy_key"))
        val response = provider.generateExplanation(AIRequest(prompt = "Testing OpenRouter"))

        assertNotNull(response)
        assertEquals("anthropic/claude-3.5-sonnet", response.modelId)
        assertTrue(response.content.contains("Testing OpenRouter"))
    }

    @Test
    fun testOllamaProviderExecution() = runBlocking {
        val provider = OllamaProvider(config = OllamaConfiguration())
        val response = provider.generateExplanation(AIRequest(prompt = "Testing Ollama"))

        assertNotNull(response)
        assertEquals("llama3", response.modelId)
        assertTrue(response.content.contains("Testing Ollama"))
    }

    @Test
    fun testOpenRouterExecuteRuntimeRequest() {
        val provider = OpenRouterProvider(config = OpenRouterConfiguration(apiKey = "dummy_key"))
        val request = AIExecutionRequest(requestId = "req_or_01", prompt = "OpenRouter Runtime test")
        val response = provider.executeRuntimeRequest(request)

        assertNotNull(response)
        assertEquals("OPENROUTER", response.provider)
        assertEquals("anthropic/claude-3.5-sonnet", response.model)
    }

    @Test
    fun testOllamaExecuteRuntimeRequest() {
        val provider = OllamaProvider(config = OllamaConfiguration())
        val request = AIExecutionRequest(requestId = "req_ollama_01", prompt = "Ollama Runtime test")
        val response = provider.executeRuntimeRequest(request)

        assertNotNull(response)
        assertEquals("OLLAMA", response.provider)
        assertEquals("llama3", response.model)
    }
}
