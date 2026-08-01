package com.learnwitme.core.domain.ai.prompt

/**
 * Normalizes rendered prompt text (trims whitespace, normalizes line endings, collapses redundant blank lines).
 * Deterministic string transformation preserving semantic content.
 */
class PromptSanitizer {

    fun sanitize(text: String): String {
        if (text.isBlank()) return ""

        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .lines()
            .joinToString("\n") { it.trimEnd() }
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}
