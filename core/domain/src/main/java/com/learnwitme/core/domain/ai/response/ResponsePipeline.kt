package com.learnwitme.core.domain.ai.response

import com.learnwitme.core.domain.ai.prompt.PromptBudget

/**
 * Pure, stateless coordinator pipeline for AI output response cleaning, normalization, JSON extraction, and validation.
 */
class ResponsePipeline(
    private val cleaner: ResponseCleaner = ResponseCleaner(),
    private val markdownNormalizer: MarkdownNormalizer = MarkdownNormalizer(),
    private val jsonExtractor: JsonResponseExtractor = JsonResponseExtractor(),
    private val validator: ResponseValidator = ResponseValidator(),
    private val budgetEstimator: PromptBudget = PromptBudget()
) {

    fun process(
        rawContent: String,
        policy: ResponseProcessingPolicy = ResponseProcessingPolicy.DEFAULT,
        processingTimeMs: Long = 0L
    ): ProcessedAIResponse {

        // 1. Cleaning Phase
        val cleaned = if (policy.enableCleaning) {
            cleaner.clean(rawContent)
        } else {
            rawContent
        }

        // 2. Markdown Normalization Phase
        val normalized = if (policy.enableMarkdownNormalization) {
            markdownNormalizer.normalize(cleaned)
        } else {
            cleaned
        }

        // 3. JSON Extraction Phase
        val jsonResult = if (policy.enableJsonExtraction) {
            jsonExtractor.extractJson(normalized)
        } else {
            JsonResponseExtractor.JsonExtractionResult(hasJson = false)
        }

        // 4. Validation Phase
        val validationResult = validator.validate(
            content = normalized,
            policy = policy,
            jsonExtractionResult = jsonResult
        )

        // 5. Metadata Construction
        val containsCode = normalized.contains("```")
        val containsMarkdown = containsCode || normalized.lines().any { line ->
            line.trimStart().let { it.startsWith("#") || it.startsWith("- ") || it.startsWith("* ") }
        }
        val estimatedTokens = budgetEstimator.estimateTokens(normalized)

        val metadata = ResponseMetadata(
            processingTimeMs = processingTimeMs,
            estimatedTokens = estimatedTokens,
            containsMarkdown = containsMarkdown,
            containsJson = jsonResult.hasJson,
            containsCodeBlock = containsCode,
            language = "en",
            normalizedCharacters = normalized.length
        )

        val accumulatedWarnings = validationResult.warnings.toMutableList()

        return ProcessedAIResponse(
            cleanContent = normalized,
            metadata = metadata,
            warnings = accumulatedWarnings,
            validationResult = validationResult,
            extractedJson = jsonResult.extractedJson
        )
    }
}
