package com.eyther.lumbridge.core.coroutines.stateflow

sealed interface CoroutineLaunchStrategy {
    /** Launches the coroutine blocks eagerly, as soon as they are defined. */
    data object Eagerly : CoroutineLaunchStrategy

    /** Launches the coroutine blocks lazily, only when there is a subscriber to the state. */
    data object Lazily : CoroutineLaunchStrategy
}
