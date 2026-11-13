package com.eyther.lumbridge.core.domain

data class HouseholdContextDomain(
    val id: String,
    val userId: String,
    val name: String,
    val currency: String,
    val timeZone: String,
    val demo: Boolean,
)

object DemoHousehold {
    const val ID = "00000000-0000-4000-8000-000000000010"
    const val USER = "00000000-0000-4000-8000-000000000001"
    const val ALEX = "00000000-0000-4000-8000-000000000002"
    const val SAM = "00000000-0000-4000-8000-000000000003"
    val context = HouseholdContextDomain(ID, USER, "The little nest", "EUR", "Europe/Lisbon", true)
}
