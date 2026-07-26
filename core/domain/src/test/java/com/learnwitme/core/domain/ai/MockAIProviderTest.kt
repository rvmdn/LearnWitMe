package com.learnwitme.core.domain.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAIProviderTest {

    @Test
    fun testDeterministicExplanationResponse() = runBlocking {
        val provider = MockAIProvider()
        val request = AIRequest(prompt = "Kotlin Coroutines")

        val res1 = provider.generateExplanation(request)
        val res2 = provider.generateExplanation(request)

        assertNotNull(res1.content)
        assertTrue(res1.content.contains("Kotlin Coroutines"))
        assertEquals(res1.content, res2.content)
    }

    @Test
    fun testFailureInjection() = runBlocking {
        val config = MockAIConfiguration(
            shouldSimulateFailure = true,
            failureException = AIProviderException.RateLimitException()
        )
        val provider = MockAIProvider(config)
        try {
            provider.generateExplanation(AIRequest(prompt = "Test"))
            org.junit.Assert.fail("Expected RateLimitException")
        } catch (e: AIProviderException.RateLimitException) {
            assertTrue(e.message!!.contains("rate limit"))
        }
    }
}
