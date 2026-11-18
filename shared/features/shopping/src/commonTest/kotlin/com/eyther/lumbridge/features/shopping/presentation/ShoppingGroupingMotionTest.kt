package com.eyther.lumbridge.features.shopping.presentation

import androidx.compose.runtime.MonotonicFrameClock
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingGroupingState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingGroupingMotionTest {
    @Test
    fun committingDoesNotResetTheFingerOffsetAndServerRollbackSettlesBack() = runTest {
        val clock =
            object : MonotonicFrameClock {
                override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                    delay(16)
                    return onFrame(testScheduler.currentTime * 1_000_000)
                }
            }
        val gesture =
            ShoppingGroupingState(CoroutineScope(backgroundScope.coroutineContext + clock), 0f)
        gesture.onStart()
        gesture.onDrag(70f, 24f, true, false)
        assertEquals(24f, gesture.position)
        assertEquals(true, gesture.onFinish(56f, 24f, true, false))
        assertEquals(
            24f,
            gesture.position,
            "Release must not jump to zero before the row becomes nested",
        )
        gesture.onCommitted(24f)
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertEquals(24f, gesture.position)
        gesture.onCommitted(0f)
        runCurrent()
        assertEquals(24f, gesture.position)
        advanceTimeBy(200)
        runCurrent()
        assertEquals(0f, gesture.position)
    }

    @Test
    fun shortOrCancelledSwipesKeepTheCommittedRelationshipAndDraggingNeverOvershoots() = runTest {
        val clock =
            object : MonotonicFrameClock {
                override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                    delay(16)
                    return onFrame(testScheduler.currentTime * 1_000_000)
                }
            }
        val gesture =
            ShoppingGroupingState(CoroutineScope(backgroundScope.coroutineContext + clock), 24f)
        gesture.onStart()
        gesture.onDrag(-20f, 24f, false, true)
        assertNull(gesture.onFinish(56f, 24f, false, true))
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertEquals(24f, gesture.position)
        gesture.onStart()
        gesture.onDrag(-400f, 24f, false, true)
        assertTrue(gesture.position in 0f..24f)
        gesture.onCancel()
        runCurrent()
        advanceTimeBy(200)
        runCurrent()
        assertEquals(24f, gesture.position)
    }
}
