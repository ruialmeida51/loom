package com.eyther.lumbridge.core.coroutines.extensions

import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Minimum first-load duration without delaying subsequent realtime updates. */
fun <T> Flow<T>.delayFirstAtLeast(timeToWaitInMillis: Long = 300L): Flow<T> = flow {
    val started = TimeSource.Monotonic.markNow()
    var first = true
    collect { value ->
        if (first) {
            val remaining = timeToWaitInMillis - started.elapsedNow().inWholeMilliseconds
            if (remaining > 0) delay(remaining)
            first = false
        }
        emit(value)
    }
}
