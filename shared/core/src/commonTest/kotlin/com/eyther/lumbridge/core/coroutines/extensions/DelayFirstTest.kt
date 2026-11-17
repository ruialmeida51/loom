package com.eyther.lumbridge.core.coroutines.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class DelayFirstTest {
    @Test
    fun intentionalLoadingDelaysOnlyTheFirstFastResult() = runTest {
        val times = mutableListOf<Long>()
        val job = launch {
            flow {
                emit(1)
                delay(25)
                emit(2)
            }
                .delayFirstAtLeast()
                .collect { times += testScheduler.currentTime }
        }
        runCurrent()
        assertTrue(times.isEmpty())
        advanceTimeBy(300)
        runCurrent()
        assertEquals(1, times.size)
        assertTrue(times.first() in 250L..300L)
        job.join()
        assertEquals(25L, times.last() - times.first())
    }

    @Test
    fun slowRequestsAreNotProlonged() = runBlocking {
        val started = TimeSource.Monotonic.markNow()
        flow {
            delay(500)
            emit(1)
        }
            .delayFirstAtLeast()
            .collect {}
        assertTrue(started.elapsedNow().inWholeMilliseconds in 500L..750L)
    }

    @Test
    fun quietResumeIsImmediate() = runTest {
        val times = mutableListOf<Long>()
        flow { emit(2) }
            .delayFirstAtLeast(enabled = false)
            .collect { times += testScheduler.currentTime }
        assertEquals(listOf(0L), times)
    }

    @Test
    fun leavingDuringTheTransitionCancelsTheEmission() = runTest {
        val values = mutableListOf<Int>()
        val job = launch { flow { emit(1) }.delayFirstAtLeast().collect { values += it } }
        runCurrent()
        advanceTimeBy(100)
        job.cancelAndJoin()
        advanceTimeBy(300)
        assertTrue(values.isEmpty())
    }
}
