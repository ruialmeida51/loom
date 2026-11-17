package com.eyther.lumbridge.features.feed.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FeedSubscriptionResponse(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("last_fetched_at") val lastFetchedAt: String? = null,
    @SerialName("last_error") val lastError: String? = null,
)
