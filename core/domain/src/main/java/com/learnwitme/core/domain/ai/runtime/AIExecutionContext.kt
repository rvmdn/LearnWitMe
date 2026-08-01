package com.learnwitme.core.domain.ai.runtime

import kotlinx.serialization.Serializable

/**
 * Immutable runtime context containing execution metadata.
 * Pure Kotlin value object; free from Android Context or framework dependencies.
 */
@Serializable
data class AIExecutionContext(
    val conversationId: String = "",
    val sessionId: String = "",
    val policyId: String = "",
    val locale: String = "en-US",
    val timezone: String = "UTC",
    val privacyMode: String = "STANDARD",
    val featureName: String = "DEFAULT"
) {
    companion object {
        val DEFAULT = AIExecutionContext()
    }
}
