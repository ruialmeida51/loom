package com.eyther.lumbridge.core.coroutines.stateflow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Collects the provided [Flow] eagerly, meaning the flow is started immediately.
 *
 * @param scope Coroutine scope in which to collect the flow.
 * @param block Lambda providing the [Flow] to collect.
 */
private fun <T> MutableStateFlow<T>.collectEagerly(
    scope: CoroutineScope,
    block: suspend (MutableStateFlow<T>) -> Flow<*>,
) {
    scope.launch { block(this@collectEagerly).collect() }
}

/**
 * Collects the provided [Flow] lazily, starting only when the first subscriber subscribes to the
 * parent [MutableStateFlow].
 *
 * @param scope Coroutine scope in which to collect the flow.
 * @param block Lambda providing the [Flow] to collect.
 */
private fun <T> MutableStateFlow<T>.collectLazily(
    scope: CoroutineScope,
    block: suspend (MutableStateFlow<T>) -> Flow<*>,
) {
    scope.launch {
        waitForFirstSubscriber()
        block(this@collectLazily).collect()
    }
}

/**
 * Collects the provided [Flow] only while there are active subscribers to the parent
 * [MutableStateFlow].
 *
 * A collection job is created when the first subscriber appears and cancelled when no subscribers
 * remain, after waiting [stopTimeoutMillis].
 *
 * @param scope Coroutine scope in which to collect the flow.
 * @param block Lambda providing the [Flow] to collect.
 * @param stopTimeoutMillis Delay before cancelling the flow after all subscribers are gone.
 */
private fun <T> MutableStateFlow<T>.collectWhileSubscribed(
    scope: CoroutineScope,
    block: suspend (MutableStateFlow<T>) -> Flow<*>,
    stopTimeoutMillis: Long,
) {
    var job: Job? = null
    var stopJob: Job? = null

    subscriptionCount
        .map { it > 0 }
        .distinctUntilChanged()
        .onEach { hasSubscribers ->
            if (hasSubscribers) {
                stopJob?.cancelAndJoin()
                stopJob = null

                // We don't want to repeat the collection if there's one active already. Checked by
                // isActive, not null: a subscriber returning while the stop job waits for the old
                // collection to finish cancels the stop job before it can null the reference, and
                // that cancelled collection must not count as running.
                if (job?.isActive != true)
                    job = scope.launch { block(this@collectWhileSubscribed).collect() }
            } else {
                // No need to check for nullability.
                // Whenever we enter this condition this is guaranteed to be null.
                stopJob = scope.launch {
                    delay(stopTimeoutMillis)
                    job?.cancelAndJoin()
                    job = null
                }
            }
        }
        .launchIn(scope)
}

/**
 * Launches a child [Flow] tied to a [MutableStateFlow]'s lifecycle, controlled by [launchStrategy].
 * This enables coordinated flow collection that automatically starts and stops with the parent
 * state flow’s active subscribers.
 *
 * Example:
 * ```
 * val state = MutableStateFlow(State())
 *     .observeWith(viewModelScope) { observeSettings() }
 *     .observeWith(viewModelScope) { observeUserData() }
 * ```
 *
 * @param scope The [CoroutineScope] used to launch the flow collectors.
 * @param launchStrategy Determines when the flow starts and stops collecting. Defaults to
 *   [FlowLaunchStrategy.WhileSubscribed].
 * @param block The suspend lambda returning a [Flow] that should be collected while the parent
 *   [MutableStateFlow] is active.
 * @return The same [MutableStateFlow] instance, allowing call chaining.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> MutableStateFlow<T>.observeWith(
    scope: CoroutineScope,
    launchStrategy: FlowLaunchStrategy = FlowLaunchStrategy.WhileSubscribed(),
    block: suspend (MutableStateFlow<T>) -> Flow<*>,
): MutableStateFlow<T> {

    when (launchStrategy) {
        is FlowLaunchStrategy.Eagerly ->
            collectEagerly(
                scope = scope,
                block = block,
            )

        is FlowLaunchStrategy.Lazily ->
            collectLazily(
                scope = scope,
                block = block,
            )

        is FlowLaunchStrategy.WhileSubscribed ->
            collectWhileSubscribed(
                scope = scope,
                block = block,
                stopTimeoutMillis = launchStrategy.stopTimeoutMillis,
            )
    }

    return this // Make sure to always return the same state flow instance.
}

/**
 * Updates the receiver [MutableStateFlow] with values emitted by this [Flow].
 *
 * Example:
 * ```
 * observeSettingsRedirect()
 *     .updateState(state) { copy(redirectTo = it) }
 * ```
 */
inline fun <T, State> Flow<T>.updateState(
    state: MutableStateFlow<State>,
    crossinline updateBlock: suspend State.(T) -> State,
): Flow<T> {
    return this.onEach { value ->
        state.update { it.updateBlock(value) }
    }
}
