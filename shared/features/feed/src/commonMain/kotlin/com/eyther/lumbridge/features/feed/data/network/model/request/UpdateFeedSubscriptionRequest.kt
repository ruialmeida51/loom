package com.eyther.lumbridge.features.feed.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateFeedSubscriptionRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_expected_url") val expectedUrl: String,
    @SerialName("p_new_id") val newId: String,
    @SerialName("p_url") val url: String,
)
