package com.learnwitme.core.domain.ai.prompt

import kotlinx.serialization.Serializable

/**
 * Immutable prompt template definition containing text templates with variable placeholders.
 */
@Serializable
data class PromptTemplate(
    val id: String,
    val name: String,
    val version: String = "1.0.0",
    val description: String = "",
    val systemTemplate: String = "",
    val userTemplate: String,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(id.isNotBlank()) { "Template id cannot be blank" }
        require(name.isNotBlank()) { "Template name cannot be blank" }
        require(userTemplate.isNotBlank()) { "userTemplate cannot be blank" }
    }

    /**
     * Extracts required variable keys enclosed in {{key}} placeholders from system and user templates.
     */
    fun extractPlaceholders(): Set<String> {
        val pattern = Regex("""\{\{([a-zA-Z0-9_.-]+)\}\}""")
        val systemKeys = pattern.findAll(systemTemplate).map { it.groupValues[1] }.toSet()
        val userKeys = pattern.findAll(userTemplate).map { it.groupValues[1] }.toSet()
        return systemKeys + userKeys
    }
}
