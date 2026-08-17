package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedLesson(
    val title: String,
    val summary: String,
    val sections: List<LessonSection> = emptyList(),
    val examples: List<String> = emptyList(),
    val estimatedReadingMinutes: Int = 3
)
