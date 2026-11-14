package com.eyther.lumbridge.features.feed.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FollowFeedRequest(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("url") val url: String,
    @SerialName("created_by") val createdBy: String,
)
