package com.learnwitme.core.application.usecase

import com.learnwitme.core.application.dispatcher.DispatcherProvider
import com.learnwitme.core.application.result.AppResult
import kotlinx.coroutines.withContext

/**
 * Responsibility: Generic abstract UseCase execution contract wrapping suspend execution
 * on background dispatchers and capturing outcomes safely inside AppResult.
 */
abstract class BaseUseCase<in P, out R>(
    private val dispatcherProvider: DispatcherProvider
) {
    protected abstract suspend fun execute(params: P): R

    suspend operator fun invoke(params: P): AppResult<R> {
        return withContext(dispatcherProvider.io) {
            try {
                AppResult.Success(execute(params))
            } catch (e: Throwable) {
                AppResult.Error(e)
            }
        }
    }
}
