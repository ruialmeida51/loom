package com.eyther.lumbridge.core.coroutines.stateflow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * Suspends until the first active subscriber appears on the [MutableStateFlow]. Used to defer flow
 * collection for lazy launch strategies.
 */
internal suspend fun <T> MutableStateFlow<T>.waitForFirstSubscriber() {
    subscriptionCount.first { subscriptions -> subscriptions > 0 }
}
