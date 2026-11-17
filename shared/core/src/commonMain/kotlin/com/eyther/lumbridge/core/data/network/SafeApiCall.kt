package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    mapError: (Exception) -> AppError = Exception::toAppError,
    action: suspend () -> T,
): AppResult<T> =
    withContext(Dispatchers.IO) {
        try {
            AppResult.Success(action())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            AppResult.Failure(mapError(error))
        }
    }

/**
 * Opt in to transport recovery only for read streams. Authorization failures and writes never
 * retry.
 */
fun <T> Flow<T>.asApiResult(
    mapError: (Exception) -> AppError = Exception::toAppError,
    retryTransientFailures: Boolean = false,
): Flow<AppResult<T>> =
    map<T, AppResult<T>> { value ->
            AppResult.Success(value)
        }
        .retryWhen { cause, attempt ->
            if (cause is CancellationException) throw cause
            val error = (cause as? Exception)?.let(mapError)
            if (
                retryTransientFailures &&
                    (error is CommonError.NoConnection || error is CommonError.ServiceUnavailable)
            ) {
                emit(AppResult.Failure(error))
                delay((attempt.coerceAtMost(4L) + 1L) * 1_000L)
                true
            } else false
        }
        .catch { error ->
            when (error) {
                is CancellationException -> throw error
                is Exception -> emit(AppResult.Failure(mapError(error)))
                else -> throw error
            }
        }
        .flowOn(Dispatchers.IO)
