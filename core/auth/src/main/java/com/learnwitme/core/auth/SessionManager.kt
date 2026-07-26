package com.learnwitme.core.auth

import com.learnwitme.core.network.AuthTokenProvider
import com.learnwitme.core.storage.EncryptedDataStore
import com.learnwitme.core.storage.local.dao.UserProfileDao
import com.learnwitme.core.storage.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class SessionManager @Inject constructor(
    private val encryptedDataStore: EncryptedDataStore,
    private val userProfileDao: UserProfileDao
) : AuthTokenProvider {

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    override fun getAccessToken(): String {
        return runBlocking {
            encryptedDataStore.getStringFlow(KEY_ACCESS_TOKEN).firstOrNull() ?: ""
        }
    }

    override fun getAccessTokenFlow(): Flow<String> {
        return encryptedDataStore.getStringFlow(KEY_ACCESS_TOKEN)
    }

    override fun refreshAccessToken(): String? {
        return runBlocking {
            val refreshToken = encryptedDataStore.getStringFlow(KEY_REFRESH_TOKEN).firstOrNull()
            if (refreshToken.isNullOrEmpty()) {
                Timber.w("No refresh token found. Unable to refresh.")
                return@runBlocking null
            }
            
            try {
                // In production, execute a synchronous API call.
                // We simulate token rotation:
                val newAccessToken = "new_access_token_${System.currentTimeMillis()}"
                val newRefreshToken = "new_refresh_token_${System.currentTimeMillis()}"
                
                encryptedDataStore.putString(KEY_ACCESS_TOKEN, newAccessToken)
                encryptedDataStore.putString(KEY_REFRESH_TOKEN, newRefreshToken)
                
                Timber.d("Refresh token rotation succeeded. New token stored.")
                newAccessToken
            } catch (e: Exception) {
                Timber.e(e, "Exception during token refresh.")
                null
            }
        }
    }

    suspend fun login(accessToken: String, refreshToken: String, profile: UserProfileEntity) {
        encryptedDataStore.putString(KEY_ACCESS_TOKEN, accessToken)
        encryptedDataStore.putString(KEY_REFRESH_TOKEN, refreshToken)
        userProfileDao.insertUserProfile(profile)
        Timber.d("User session stored. Logged in successfully for user: %s", profile.userId)
    }

    suspend fun logout() {
        encryptedDataStore.clear()
        Timber.d("Session cleared. Logged out successfully.")
    }

    fun isUserLoggedInFlow(): Flow<Boolean> {
        return getAccessTokenFlow().map { it.isNotEmpty() }
    }
}
