package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class LessonSection(
    val title: String,
    val content: String
)
