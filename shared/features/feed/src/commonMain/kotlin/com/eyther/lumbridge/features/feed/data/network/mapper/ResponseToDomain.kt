package com.eyther.lumbridge.features.feed.data.network.mapper

import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain

internal fun ArticleResponse.toDomain(): ArticleDomain = ArticleDomain(
    id = id,
    title = title,
    summary = summary,
    url = url,
    source = source,
    publishedAt = publishedAt,
    read = read,
)
