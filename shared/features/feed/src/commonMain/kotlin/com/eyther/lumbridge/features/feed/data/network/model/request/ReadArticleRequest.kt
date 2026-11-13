package com.eyther.lumbridge.features.feed.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReadArticleRequest(
    @SerialName("article_id") val articleId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("is_read") val read: Boolean,
)
