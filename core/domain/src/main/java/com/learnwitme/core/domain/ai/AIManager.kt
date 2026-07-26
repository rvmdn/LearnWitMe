package com.learnwitme.core.domain.ai

/**
 * Responsibility: High-level orchestration facade that receives AIRequest objects, delegates
 * provider selection to AIProviderRouter, and executes requests.
 */
class AIManager(
    private val router: AIProviderRouter
) {
    suspend fun generateExplanation(
        request: AIRequest,
        policy: AIProviderSelectionPolicy = AIProviderSelectionPolicy()
    ): AIResponse {
        val provider = router.resolveProvider(policy)
        return provider.generateExplanation(request)
    }

    suspend fun generateLesson(
        request: AIRequest,
        policy: AIProviderSelectionPolicy = AIProviderSelectionPolicy()
    ): AIResponse {
        val provider = router.resolveProvider(policy)
        return provider.generateLesson(request)
    }

    suspend fun generateQuiz(
        request: AIRequest,
        policy: AIProviderSelectionPolicy = AIProviderSelectionPolicy()
    ): AIResponse {
        val provider = router.resolveProvider(policy)
        return provider.generateQuiz(request)
    }

    suspend fun simplifyContent(
        request: AIRequest,
        policy: AIProviderSelectionPolicy = AIProviderSelectionPolicy()
    ): AIResponse {
        val provider = router.resolveProvider(policy)
        return provider.simplifyContent(request)
    }
}
