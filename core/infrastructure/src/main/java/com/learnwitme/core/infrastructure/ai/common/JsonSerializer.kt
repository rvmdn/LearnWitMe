package com.learnwitme.core.infrastructure.ai.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Shared JSON serialization wrapper.
 */
class JsonSerializer(
    val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) {

    fun <T> encodeToString(serializer: KSerializer<T>, value: T): String {
        return json.encodeToString(serializer, value)
    }

    fun <T> decodeFromString(serializer: KSerializer<T>, string: String): T {
        return json.decodeFromString(serializer, string)
    }
}
