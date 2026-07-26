package com.learnwitme.core.network

import kotlinx.coroutines.flow.Flow

interface AuthTokenProvider {
    fun getAccessToken(): String
    fun getAccessTokenFlow(): Flow<String>
    fun refreshAccessToken(): String?
}
