package com.learnwitme.core.domain.ai.response

/**
 * Pure deterministic cleaner for raw AI output text.
 */
class ResponseCleaner {

    fun clean(rawContent: String): String {
        if (rawContent.isBlank()) return ""

        return rawContent
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("""[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]"""), "")
            .lines()
            .joinToString("\n") { it.trimEnd() }
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}
