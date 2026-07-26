package com.learnwitme.core.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AIProviderRouterTest {

    private val registry = AIProviderRegistry()
    private val router = AIProviderRouter(registry)
    private val mockProvider = MockAIProvider()

    @Test
    fun testResolvePreferredProvider() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
        val policy = AIProviderSelectionPolicy(preferredProviderType = AIProviderType.MOCK)

        val resolved = router.resolveProvider(policy)
        assertEquals(mockProvider, resolved)
    }

    @Test
    fun testResolveFallbackProvider() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
        val policy = AIProviderSelectionPolicy(
            preferredProviderType = AIProviderType.GEMINI,
            fallbackProviderType = AIProviderType.MOCK,
            allowFallback = true
        )

        val resolved = router.resolveProvider(policy)
        assertEquals(mockProvider, resolved)
    }

    @Test
    fun testThrowExceptionWhenNoProviderFound() {
        val policy = AIProviderSelectionPolicy(
            preferredProviderType = AIProviderType.GEMINI,
            allowFallback = false
        )
        try {
            router.resolveProvider(policy)
            org.junit.Assert.fail("Expected ModelNotFoundException")
        } catch (e: AIProviderException.ModelNotFoundException) {
            org.junit.Assert.assertTrue(e.message!!.contains("No active AIProvider registered"))
        }
    }
}
