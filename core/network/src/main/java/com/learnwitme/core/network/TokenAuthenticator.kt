package com.learnwitme.core.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: Provider<AuthTokenProvider>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            val provider = tokenProvider.get()
            val currentToken = provider.getAccessToken()

            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (requestToken != currentToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            Timber.d("Unauthorized call (401) detected. Attempting to refresh access token...")
            val newToken = provider.refreshAccessToken()

            return if (newToken != null) {
                Timber.d("Token refresh succeeded. Retrying request with new token.")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                Timber.w("Token refresh failed. Aborting request.")
                null
            }
        }
    }
}
