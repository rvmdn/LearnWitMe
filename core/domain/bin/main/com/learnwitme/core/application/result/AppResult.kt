package com.learnwitme.core.application.result

/**
 * Responsibility: Platform-independent sealed result representation for application operations.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
}
