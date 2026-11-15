package com.eyther.lumbridge.app.navigation.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PendingPush {
    private val mutable = MutableStateFlow<PushDestination?>(null)
    val destination = mutable.asStateFlow()

    fun open(householdId: String, route: String, accountId: String? = null) {
        if (
            Regex("[0-9a-fA-F-]{36}").matches(householdId) &&
                route in listOf("money", "reminders", "insights")
        )
            mutable.value =
                PushDestination(
                    householdId = householdId,
                    route = route,
                    accountId = accountId?.takeIf { Regex("[0-9a-fA-F-]{36}").matches(it) },
                )
    }

    fun clear() {
        mutable.value = null
    }
}
