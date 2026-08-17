package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val question: String,
    val options: List<QuizOption>,
    val correctAnswer: String,
    val explanation: String = ""
)
