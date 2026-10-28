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

    @Test
    fun testPromptSanitizationWithContextTag() = runBlocking {
        val provider = MockAIProvider()
        val request = AIRequest(
            prompt = """
                === CURRENT REQUEST ===
                Explain the reasoning for the question: 'Explain core concepts, practical examples, and common pitfalls of Null Safety & Nullable Types'.
                Selected Answer: 'Sistem tipe Kotlin membedakan tipe yang dapat bernilai null (Tipe?) dari tipe non-null (Tipe).'
                Expected Answer: 'Mencegah NullPointerException (Kesalahan Bernilai Miliar Dolar) langsung pada saat kompilasi.'
            """.trimIndent()
        )

        val res = provider.generateExplanation(request)
        org.junit.Assert.assertFalse(res.content.contains("=== CURRENT REQUEST ==="))
        org.junit.Assert.assertFalse(res.content.contains("Selected Answer:"))
        assertTrue(res.content.contains("Null Safety & Nullable Types"))
    }
}
