package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false
)
