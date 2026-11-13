package com.eyther.lumbridge.features.feed.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshFeedRequest(
    @SerialName("subscription_id") val subscriptionId: String,
)
