package com.learnwitme.core.domain.ai.prompt

/**
 * High-level coordinator pipeline for prompt construction, validation, sanitization, and token budget estimation.
 * Pure Kotlin, deterministic execution with zero side-effects.
 */
class PromptPipeline(
    private val validator: PromptValidator = PromptValidator(),
    private val builder: PromptBuilder = PromptBuilder(),
    private val sanitizer: PromptSanitizer = PromptSanitizer(),
    private val budget: PromptBudget = PromptBudget()
) {

    fun execute(request: PromptBuildRequest): PromptBuildResult {
        // 1. Validation Phase
        val validationResult = validator.validate(request)

        // 2. Build Phase (placeholder substitution)
        val rendered = builder.build(request.template, request.variables)

        // 3. Sanitization Phase (whitespace & line-ending normalization)
        val sanitizedSystem = sanitizer.sanitize(rendered.renderedSystemPrompt)
        val sanitizedUser = sanitizer.sanitize(rendered.renderedUserPrompt)

        // 4. Budget Estimation Phase
        val maxTokensAllowed = request.executionPolicy.timeoutMs.toInt().coerceAtLeast(2048)
        val budgetEvaluation = budget.evaluate(
            systemPrompt = sanitizedSystem,
            userPrompt = sanitizedUser,
            maxTokenLimit = maxTokensAllowed
        )

        // Combine warnings and error messages safely
        val warnings = mutableListOf<String>()
        warnings.addAll(validationResult.warnings)
        validationResult.errors.forEach { warnings.add("ERROR: $it") }
        budgetEvaluation.warning?.let { warnings.add(it) }

        // Metadata assembly
        val mergedMetadata = request.template.metadata.toMutableMap()
        mergedMetadata["templateId"] = request.template.id
        mergedMetadata["templateVersion"] = request.template.version
        mergedMetadata["featureName"] = request.executionContext.featureName

        return PromptBuildResult(
            renderedSystemPrompt = sanitizedSystem,
            renderedUserPrompt = sanitizedUser,
            estimatedTokenCount = budgetEvaluation.estimatedTokens,
            warnings = warnings,
            metadata = mergedMetadata,
            isValid = validationResult.isValid
        )
    }
}
