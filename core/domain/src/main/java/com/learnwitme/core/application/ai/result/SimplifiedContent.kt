package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class SimplifiedContent(
    val original: String,
    val simplified: String,
    val readingLevel: String = "BEGINNER"
)
