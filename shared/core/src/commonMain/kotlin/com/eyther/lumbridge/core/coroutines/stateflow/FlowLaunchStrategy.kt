package com.eyther.lumbridge.core.coroutines.stateflow

sealed interface FlowLaunchStrategy {
    companion object {
        /**
         * Default timeout used by [FlowLaunchStrategy.WhileSubscribed]. Indicates how long to keep
         * active collectors alive after the last subscriber is gone.
         */
        const val DEFAULT_TIME_OUT_MILLIS = 5_000L
    }

    /** Launches flows blocks eagerly, as soon as they are defined. */
    data object Eagerly : FlowLaunchStrategy

    /** Launches flows blocks lazily, only when there is a subscriber to the state. */
    data object Lazily : FlowLaunchStrategy

    /**
     * Launches flows blocks while there are active subscribers to the state.
     *
     * @param stopTimeoutMillis The timeout in milliseconds to wait before stopping the flows after
     *   the last subscriber unsubscribes.
     */
    data class WhileSubscribed(val stopTimeoutMillis: Long = DEFAULT_TIME_OUT_MILLIS) :
        FlowLaunchStrategy
}
