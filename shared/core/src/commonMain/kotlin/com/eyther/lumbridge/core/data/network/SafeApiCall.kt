package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    mapError: (Exception) -> AppError = Exception::toAppError,
    action: suspend () -> T,
): AppResult<T> = withContext(Dispatchers.IO) {
    try {
        AppResult.Success(action())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        AppResult.Failure(mapError(error))
    }
}

fun <T> Flow<T>.asApiResult(
    mapError: (Exception) -> AppError = Exception::toAppError,
): Flow<AppResult<T>> = map<T, AppResult<T>> { value ->
    AppResult.Success(value)
}.catch { error ->
    when (error) {
        is CancellationException -> throw error
        is Exception -> emit(AppResult.Failure(mapError(error)))
        else -> throw error
    }
}.flowOn(Dispatchers.IO)
