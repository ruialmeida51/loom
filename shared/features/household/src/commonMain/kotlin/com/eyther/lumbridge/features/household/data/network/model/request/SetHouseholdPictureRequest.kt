package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A null [path] clears the picture. */
@Serializable
internal data class SetHouseholdPictureRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_path") val path: String?,
)
