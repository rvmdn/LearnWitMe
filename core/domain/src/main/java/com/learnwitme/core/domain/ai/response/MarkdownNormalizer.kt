package com.learnwitme.core.domain.ai.response

/**
 * Pure deterministic normalizer for Markdown formatting in AI responses.
 */
class MarkdownNormalizer {

    fun normalize(content: String): String {
        if (content.isBlank()) return ""

        val lines = content.lines()
        val normalizedLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trimStart()

            val normalizedLine = when {
                trimmed.startsWith("* ") -> "- " + trimmed.substring(2)
                trimmed.startsWith("+ ") -> "- " + trimmed.substring(2)
                trimmed.startsWith("#") -> {
                    val hashCount = trimmed.takeWhile { it == '#' }.length
                    val rest = trimmed.substring(hashCount).trimStart()
                    "#".repeat(hashCount) + " " + rest
                }
                else -> line
            }
            normalizedLines.add(normalizedLine)
        }

        return normalizedLines.joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}
