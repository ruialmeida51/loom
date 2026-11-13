package com.eyther.lumbridge.core.data

import com.eyther.lumbridge.core.domain.PushDestinationDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PendingPush {
    private val mutable = MutableStateFlow<PushDestinationDomain?>(null)
    val destination = mutable.asStateFlow()

    fun open(householdId: String, route: String) {
        if (
            Regex("[0-9a-fA-F-]{36}").matches(householdId) &&
                route in listOf("money", "reminders", "insights")
        )
            mutable.value = PushDestinationDomain(householdId, route)
    }

    fun clear() {
        mutable.value = null
    }
}
