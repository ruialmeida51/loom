package com.eyther.lumbridge.core.domain

import kotlinx.coroutines.CancellationException

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val message: String) : AppResult<Nothing>
}

suspend fun <T> safeCall(action: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(action())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        AppResult.Failure(
            error.message?.substringBefore("\n")?.take(220)
                ?: "Something went wrong. Please try again.",
        )
    }

fun <T> AppResult<T>.getOrThrow(): T =
    when (this) {
        is AppResult.Success -> value
        is AppResult.Failure -> throw IllegalStateException(message)
    }
