package com.learnwitme.core.domain.ai.prompt

/**
 * Pure builder responsible for resolving variables and rendering template placeholders into final strings.
 * Deterministic with zero side-effects.
 */
class PromptBuilder {

    fun build(template: PromptTemplate, variables: PromptVariables): RenderedPrompt {
        val renderedSystem = renderText(template.systemTemplate, variables)
        val renderedUser = renderText(template.userTemplate, variables)
        return RenderedPrompt(
            renderedSystemPrompt = renderedSystem,
            renderedUserPrompt = renderedUser
        )
    }

    private fun renderText(templateText: String, variables: PromptVariables): String {
        if (templateText.isBlank()) return ""
        val pattern = Regex("""\{\{([a-zA-Z0-9_.-]+)\}\}""")
        return pattern.replace(templateText) { matchResult ->
            val key = matchResult.groupValues[1]
            variables.get(key) ?: matchResult.value
        }
    }

    data class RenderedPrompt(
        val renderedSystemPrompt: String,
        val renderedUserPrompt: String
    )
}
