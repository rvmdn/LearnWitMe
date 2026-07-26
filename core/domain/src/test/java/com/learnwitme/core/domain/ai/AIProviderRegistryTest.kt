package com.learnwitme.core.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AIProviderRegistryTest {

    private val registry = AIProviderRegistry()
    private val mockProvider = MockAIProvider()

    @Test
    fun testRegisterAndLookupProvider() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
        assertTrue(registry.hasProvider(AIProviderType.MOCK))
        assertEquals(mockProvider, registry.getProvider(AIProviderType.MOCK))
    }

    @Test
    fun testUnregisterProvider() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
        registry.unregisterProvider(AIProviderType.MOCK)
        assertFalse(registry.hasProvider(AIProviderType.MOCK))
        assertNull(registry.getProvider(AIProviderType.MOCK))
    }

    @Test
    fun testClearRegistry() {
        registry.registerProvider(AIProviderType.MOCK, mockProvider)
        registry.clear()
        assertFalse(registry.hasProvider(AIProviderType.MOCK))
    }
}
