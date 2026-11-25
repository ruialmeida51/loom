package com.eyther.lumbridge.features.household.domain.model

data class HouseholdDomain(
    val id: String,
    val name: String,
    val currency: String,
    val timeZone: String,
    /** The household's stored picture, or null for its initial badge. */
    val imagePath: String? = null,
)
