package com.eyther.lumbridge.core.domain.wrapper

import com.eyther.lumbridge.core.domain.errors.AppError

sealed interface AppResult<out T> {

    data class Success<out T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

suspend inline fun <T> safeCall(action: () -> T): AppResult<T> = try {
    AppResult.Success(action())
} catch (error: AppError) {
    AppResult.Failure(error)
}

fun <T> AppResult<T>.getOrThrow(): T = when (this) {
    is AppResult.Success -> value
    is AppResult.Failure -> throw error
}

inline fun <T, R> AppResult<T>.mapToResult(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.andThen(execute: (T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Success -> execute(value)
    is AppResult.Failure -> this
}
