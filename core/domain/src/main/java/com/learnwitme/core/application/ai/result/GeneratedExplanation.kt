package com.learnwitme.core.application.ai.result

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedExplanation(
    val title: String,
    val content: String,
    val references: List<String> = emptyList()
)
