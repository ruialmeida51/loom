package com.eyther.lumbridge.core.coroutines.stateflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Launches the provided [block] eagerly, meaning it is started immediately when this function is
 * called, regardless of subscriber count.
 *
 * @param scope Coroutine scope in which to execute the [block].
 * @param block Suspend lambda that operates on the [MutableStateFlow].
 */
private fun <T, R> MutableStateFlow<T>.launchEagerly(
    scope: CoroutineScope,
    block: suspend (MutableStateFlow<T>) -> R,
) {
    scope.launch { block(this@launchEagerly) }
}

/**
 * Launches the provided [block] lazily, waiting until the first subscriber subscribes to this
 * [MutableStateFlow] before execution begins.
 *
 * @param scope Coroutine scope in which to execute the [block].
 * @param block Suspend lambda that operates on the [MutableStateFlow].
 */
private fun <T, R> MutableStateFlow<T>.launchLazily(
    scope: CoroutineScope,
    block: suspend (MutableStateFlow<T>) -> R,
) {
    scope.launch {
        waitForFirstSubscriber()
        block(this@launchLazily)
    }
}

/**
 * Launches a coroutine tied to the lifecycle of a [MutableStateFlow], controlled by the provided
 * [launchStrategy].
 *
 * This function is similar to [observeWith], but instead of collecting a child [Flow], it launches
 * a suspending operation or effect that runs based on the chosen strategy.
 *
 * Example:
 * ```
 * val state = MutableStateFlow(State())
 *     .launchWith(viewModelScope) { flow ->
 *         performInitialLoad()
 *     }
 * ```
 *
 * @param scope The [CoroutineScope] used to launch the coroutine.
 * @param launchStrategy Determines when the coroutine starts. Defaults to
 *   [CoroutineLaunchStrategy.Lazily].
 * @param block The suspend lambda to execute when launched.
 * @return The same [MutableStateFlow] instance, allowing call chaining.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> MutableStateFlow<T>.launchWith(
    scope: CoroutineScope,
    launchStrategy: CoroutineLaunchStrategy = CoroutineLaunchStrategy.Lazily,
    block: suspend (MutableStateFlow<T>) -> R,
): MutableStateFlow<T> {

    when (launchStrategy) {
        is CoroutineLaunchStrategy.Eagerly ->
            launchEagerly(
                scope = scope,
                block = block,
            )

        is CoroutineLaunchStrategy.Lazily ->
            launchLazily(
                scope = scope,
                block = block,
            )
    }

    return this // Always return the same StateFlow instance for chaining.
}
