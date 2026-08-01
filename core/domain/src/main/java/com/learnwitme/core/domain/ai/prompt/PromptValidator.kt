package com.learnwitme.core.domain.ai.prompt

import kotlinx.serialization.Serializable

/**
 * Validates prompt build requests for missing variables, empty content, and invalid configurations.
 * Deterministic and exception-safe.
 */
class PromptValidator {

    @Serializable
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val missingVariables: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    ) {
        companion object {
            val SUCCESS = ValidationResult(isValid = true)
        }
    }

    fun validate(request: PromptBuildRequest): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val missingVars = mutableListOf<String>()

        val template = request.template
        val variables = request.variables

        if (template.userTemplate.isBlank()) {
            errors.add("User template cannot be blank")
        }

        val placeholders = template.extractPlaceholders()
        for (placeholder in placeholders) {
            if (!variables.contains(placeholder)) {
                missingVars.add(placeholder)
                warnings.add("Unresolved template placeholder: {{$placeholder}}")
            }
        }

        val isValid = errors.isEmpty()
        return ValidationResult(
            isValid = isValid,
            errors = errors,
            missingVariables = missingVars,
            warnings = warnings
        )
    }
}
