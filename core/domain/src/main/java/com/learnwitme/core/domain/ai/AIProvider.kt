package com.learnwitme.core.domain.ai

/**
 * Responsibility: Pure domain interface abstraction for AI operations. Allows the application
 * to generate explanations, lessons, quizzes, and simplified content from any LLM backend
 * without coupling to concrete HTTP/API clients or third-party SDKs.
 */
interface AIProvider {
    suspend fun generateExplanation(request: AIRequest): AIResponse
    suspend fun generateLesson(request: AIRequest): AIResponse
    suspend fun generateQuiz(request: AIRequest): AIResponse
    suspend fun simplifyContent(request: AIRequest): AIResponse
}
