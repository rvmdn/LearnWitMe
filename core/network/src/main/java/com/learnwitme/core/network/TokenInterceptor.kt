package com.learnwitme.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenInterceptor @Inject constructor(
    private val tokenProvider: Provider<AuthTokenProvider>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        if (originalRequest.header("No-Authentication") != null) {
            return chain.proceed(originalRequest.newBuilder().removeHeader("No-Authentication").build())
        }

        val token = tokenProvider.get().getAccessToken()
        return if (token.isNotEmpty()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
