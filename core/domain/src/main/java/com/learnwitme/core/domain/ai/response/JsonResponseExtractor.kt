package com.learnwitme.core.domain.ai.response

import kotlinx.serialization.Serializable

/**
 * Pure deterministic JSON block extractor for raw or markdown-wrapped AI output.
 */
class JsonResponseExtractor {

    @Serializable
    data class JsonExtractionResult(
        val hasJson: Boolean,
        val extractedJson: String? = null,
        val isWrappedInMarkdown: Boolean = false
    )

    fun extractJson(content: String): JsonExtractionResult {
        if (content.isBlank()) return JsonExtractionResult(hasJson = false)

        val jsonBlockRegex = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""", RegexOption.IGNORE_CASE)
        val match = jsonBlockRegex.find(content)
        if (match != null) {
            val extracted = match.groupValues[1].trim()
            if (isValidJsonStructure(extracted)) {
                return JsonExtractionResult(
                    hasJson = true,
                    extractedJson = extracted,
                    isWrappedInMarkdown = true
                )
            }
        }

        val trimmed = content.trim()
        if (isValidJsonStructure(trimmed)) {
            return JsonExtractionResult(
                hasJson = true,
                extractedJson = trimmed,
                isWrappedInMarkdown = false
            )
        }

        return JsonExtractionResult(hasJson = false)
    }

    private fun isValidJsonStructure(text: String): Boolean {
        val t = text.trim()
        return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))
    }
}
