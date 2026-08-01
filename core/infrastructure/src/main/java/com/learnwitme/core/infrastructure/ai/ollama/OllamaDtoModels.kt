package com.learnwitme.core.infrastructure.ai.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = false,
    val options: OllamaOptions? = null
)

@Serializable
data class OllamaOptions(
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("num_predict") val numPredict: Int? = null
)

@Serializable
data class OllamaGenerateResponse(
    val model: String? = null,
    val response: String? = null,
    val done: Boolean = true,
    @SerialName("prompt_eval_count") val promptEvalCount: Int = 0,
    @SerialName("eval_count") val evalCount: Int = 0
)
