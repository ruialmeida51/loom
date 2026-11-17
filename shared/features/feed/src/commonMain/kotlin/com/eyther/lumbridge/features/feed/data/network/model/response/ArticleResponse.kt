package com.eyther.lumbridge.features.feed.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ArticleResponse(
    @SerialName("id") val id: String,
    @SerialName("subscription_id") val subscriptionId: String,
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String,
    @SerialName("url") val url: String,
    @SerialName("source") val source: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("is_read") val read: Boolean,
)
