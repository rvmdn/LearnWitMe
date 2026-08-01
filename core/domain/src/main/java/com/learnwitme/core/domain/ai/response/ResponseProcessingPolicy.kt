package com.learnwitme.core.domain.ai.response

import kotlinx.serialization.Serializable

/**
 * Policy configuring response processing options (cleaning, markdown normalization, JSON extraction, validation bounds).
 */
@Serializable
data class ResponseProcessingPolicy(
    val enableCleaning: Boolean = true,
    val enableMarkdownNormalization: Boolean = true,
    val enableJsonExtraction: Boolean = true,
    val minContentLength: Int = 1,
    val maxContentLength: Int = 100_000,
    val requireValidJson: Boolean = false
) {
    init {
        require(minContentLength >= 0) { "minContentLength cannot be negative" }
        require(maxContentLength >= minContentLength) { "maxContentLength must be >= minContentLength" }
    }

    companion object {
        val DEFAULT = ResponseProcessingPolicy()
        val STRICT = ResponseProcessingPolicy(
            enableCleaning = true,
            enableMarkdownNormalization = true,
            enableJsonExtraction = true,
            minContentLength = 10,
            maxContentLength = 50_000,
            requireValidJson = true
        )
    }
}
