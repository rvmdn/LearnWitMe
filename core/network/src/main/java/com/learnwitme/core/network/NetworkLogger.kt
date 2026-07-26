package com.learnwitme.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLogger @Inject constructor() {
    private val _logs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val logs: StateFlow<List<NetworkLog>> = _logs.asStateFlow()

    data class NetworkLog(
        val timestamp: Long,
        val method: String,
        val url: String,
        val statusCode: Int,
        val durationMs: Long,
        val isMocked: Boolean
    )

    fun logRequest(method: String, url: String, statusCode: Int, durationMs: Long, isMocked: Boolean) {
        val newLog = NetworkLog(
            timestamp = System.currentTimeMillis(),
            method = method,
            url = url,
            statusCode = statusCode,
            durationMs = durationMs,
            isMocked = isMocked
        )
        _logs.value = (listOf(newLog) + _logs.value).take(30)
    }
}
