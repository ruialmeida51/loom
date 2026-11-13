package com.eyther.lumbridge.features.household.data.demo

import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain

/** Sample household data belongs to the household feature, outside its production domain. */
object DemoHousehold {
    const val ID = "00000000-0000-4000-8000-000000000010"
    const val USER = "00000000-0000-4000-8000-000000000001"
    const val ALEX = "00000000-0000-4000-8000-000000000002"
    const val SAM = "00000000-0000-4000-8000-000000000003"
    val context = HouseholdContextDomain(ID, USER, "The little nest", "EUR", "Europe/Lisbon", true)
}
