package com.learnwitme.core.application.ai.result

import com.learnwitme.core.domain.model.DifficultyLevel
import kotlinx.serialization.Serializable

@Serializable
data class GeneratedQuiz(
    val title: String,
    val questions: List<QuizQuestion>,
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val estimatedDurationMinutes: Int = 5
)
