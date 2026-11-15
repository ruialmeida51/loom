package com.eyther.lumbridge.app.navigation.push

data class PushDestination(
    val householdId: String,
    val route: String,
    val accountId: String? = null,
)
