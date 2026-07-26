package com.learnwitme.core.network

import com.learnwitme.core.storage.EncryptedDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockServerInterceptor @Inject constructor(
    private val encryptedDataStore: EncryptedDataStore,
    private val networkLogger: NetworkLogger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val urlString = request.url.toString()
        
        val isMockEnabled = runBlocking {
            encryptedDataStore.getStringFlow("dev_mock_api", "false").firstOrNull() == "true"
        }

        val startTime = System.nanoTime()

        if (isMockEnabled) {
            val mockJson = getMockResponseJson(urlString)
            val durationMs = (System.nanoTime() - startTime) / 1_000_000

            networkLogger.logRequest(
                method = request.method,
                url = urlString,
                statusCode = 200,
                durationMs = durationMs,
                isMocked = true
            )

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(mockJson.toResponseBody("application/json".toMediaType()))
                .build()
        }

        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startTime) / 1_000_000
            
            networkLogger.logRequest(
                method = request.method,
                url = urlString,
                statusCode = response.code,
                durationMs = durationMs,
                isMocked = false
            )
            response
        } catch (e: Exception) {
            val durationMs = (System.nanoTime() - startTime) / 1_000_000
            networkLogger.logRequest(
                method = request.method,
                url = urlString,
                statusCode = 500,
                durationMs = durationMs,
                isMocked = false
            )
            throw e
        }
    }

    private fun getMockResponseJson(url: String): String {
        return when {
            url.contains("auth/login") || url.contains("login") -> """
                {
                  "status": "success",
                  "token": "mocked_jwt_bearer_token_token12345",
                  "refreshToken": "mocked_refresh_token_67890",
                  "user": {
                    "id": "usr_mock_99",
                    "name": "Mock Developer",
                    "email": "dev@learnwitme.com",
                    "isPremium": true
                  }
                }
            """.trimIndent()
            url.contains("users/profile") || url.contains("profile") -> """
                {
                  "status": "success",
                  "user": {
                    "id": "usr_mock_99",
                    "name": "Mock Developer Updated",
                    "email": "dev@learnwitme.com",
                    "isPremium": true
                  }
                }
            """.trimIndent()
            else -> """
                {
                  "status": "success",
                  "message": "Offline mock server responder operational.",
                  "mockedUrl": "$url"
                }
            """.trimIndent()
        }
    }
}
