package com.eyther.lumbridge.core.data.local

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow

/** Short-lived, in-memory read reuse. Keys must include the current user and owning scope. */
class ReferenceSnapshot<T>(
    private val maxAge: Duration = 30.seconds,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    private data class Entry<T>(val key: String, val value: T, val writtenAt: TimeMark)

    private val entry = MutableStateFlow<Entry<T>?>(null)

    fun get(key: String): T? =
        entry.value?.takeIf { it.key == key && it.writtenAt.elapsedNow() < maxAge }?.value

    fun put(key: String, value: T) {
        entry.value = Entry(key = key, value = value, writtenAt = timeSource.markNow())
    }

    fun clear() {
        entry.value = null
    }
}
