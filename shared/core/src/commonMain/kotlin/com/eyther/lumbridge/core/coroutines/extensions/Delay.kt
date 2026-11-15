package com.eyther.lumbridge.core.coroutines.extensions

import kotlin.time.TimeSource
import kotlinx.coroutines.delay

/** Plannot's minimum loading duration, using monotonic time and preserving cancellation. */
suspend fun <R> delayAtLeast(
    timeToWaitInMillis: Long = 300L,
    block: suspend () -> R,
): R {
    val started = TimeSource.Monotonic.markNow()
    val result = block()
    val remaining = timeToWaitInMillis - started.elapsedNow().inWholeMilliseconds
    if (remaining > 0) delay(remaining)
    return result
}
