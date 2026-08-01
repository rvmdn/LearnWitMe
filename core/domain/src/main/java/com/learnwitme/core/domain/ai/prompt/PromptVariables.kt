package com.learnwitme.core.domain.ai.prompt

import kotlinx.serialization.Serializable

/**
 * Immutable variable container for prompt template placeholder substitution.
 */
@Serializable
data class PromptVariables(
    val variables: Map<String, String> = emptyMap()
) {
    fun get(key: String): String? = variables[key]

    fun getOrDefault(key: String, defaultValue: String): String = variables[key] ?: defaultValue

    fun contains(key: String): Boolean = variables.containsKey(key)

    fun merge(other: PromptVariables): PromptVariables {
        return PromptVariables(variables = this.variables + other.variables)
    }

    fun merge(otherMap: Map<String, String>): PromptVariables {
        return PromptVariables(variables = this.variables + otherMap)
    }

    companion object {
        val EMPTY = PromptVariables(emptyMap())

        fun from(map: Map<String, Any?>): PromptVariables {
            val stringMap = map.entries.associate { (k, v) -> k to (v?.toString() ?: "") }
            return PromptVariables(stringMap)
        }
    }
}
