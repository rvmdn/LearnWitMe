package com.learnwitme.core.domain.ai

/**
 * Responsibility: Deterministic implementation of AIProvider interface suitable for local development,
 * preview environments, and unit testing without network or external LLM dependencies.
 */
class MockAIProvider(
    private val config: MockAIConfiguration = MockAIConfiguration(),
    private val factory: DefaultAIResponseFactory = DefaultAIResponseFactory()
) : AIProvider {

    private fun checkFailureSimulation() {
        if (config.shouldSimulateFailure) {
            throw config.failureException ?: AIProviderException.GenericAIException("Simulated Mock AI failure")
        }
    }

    override suspend fun generateExplanation(request: AIRequest): AIResponse {
        checkFailureSimulation()
        return factory.createExplanationResponse(request, config)
    }

    override suspend fun generateLesson(request: AIRequest): AIResponse {
        checkFailureSimulation()
        return factory.createLessonResponse(request, config)
    }

    override suspend fun generateQuiz(request: AIRequest): AIResponse {
        checkFailureSimulation()
        return factory.createQuizResponse(request, config)
    }

    override suspend fun simplifyContent(request: AIRequest): AIResponse {
        checkFailureSimulation()
        return factory.createSimplifiedContentResponse(request, config)
    }
}
