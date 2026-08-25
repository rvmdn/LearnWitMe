package com.learnwitme.core.domain.model

/**
 * Responsibility: Abstract representation of user identity state within the domain.
 * Supports Guest mode without authentication/network dependency while leaving room
 * for future authentication without architectural rewrites.
 */
sealed interface UserIdentity {
    val id: UserId

    data class Guest(
        override val id: UserId = DEFAULT_GUEST_ID
    ) : UserIdentity {
        companion object {
            val DEFAULT_GUEST_ID = UserId("current_user")
        }
    }

    data class Authenticated(
        override val id: UserId,
        val email: String? = null
    ) : UserIdentity
}
