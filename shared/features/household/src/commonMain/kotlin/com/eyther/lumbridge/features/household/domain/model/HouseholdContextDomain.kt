package com.eyther.lumbridge.features.household.domain.model

data class HouseholdContextDomain(
    val id: String,
    val userId: String,
    val name: String,
    val currency: String,
    val timeZone: String,
    val demo: Boolean,
)
