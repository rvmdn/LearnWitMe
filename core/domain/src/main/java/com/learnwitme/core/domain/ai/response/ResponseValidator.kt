package com.learnwitme.core.domain.ai.response

/**
 * Pure exception-safe validator for processed AI responses against policy criteria.
 */
class ResponseValidator {

    fun validate(
        content: String,
        policy: ResponseProcessingPolicy = ResponseProcessingPolicy.DEFAULT,
        jsonExtractionResult: JsonResponseExtractor.JsonExtractionResult? = null
    ): ResponseValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (content.isBlank()) {
            errors.add("Response content is empty or blank")
        } else {
            if (content.length < policy.minContentLength) {
                errors.add("Response content length (${content.length}) is below minimum (${policy.minContentLength})")
            }
            if (content.length > policy.maxContentLength) {
                errors.add("Response content length (${content.length}) exceeds maximum allowed (${policy.maxContentLength})")
            }

            val backtickCount = content.split("```").size - 1
            if (backtickCount % 2 != 0) {
                warnings.add("Response contains an unclosed markdown code block (odd number of ``` fences)")
            }

            if (policy.requireValidJson) {
                if (jsonExtractionResult == null || !jsonExtractionResult.hasJson) {
                    errors.add("Policy requires valid JSON response, but no JSON block was found")
                }
            }
        }

        return ResponseValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
