package com.eyther.lumbridge.features.feed.data.network.mapper

import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import kotlin.time.Instant

internal fun ArticleResponse.toDomain(): ArticleDomain =
    ArticleDomain(
        id = id,
        title = title,
        summary = summary,
        url = url,
        source = source,
        publishedAt = Instant.parse(publishedAt),
        read = read,
        subscriptionId = subscriptionId,
        imageUrl = imageUrl?.takeIf { it.startsWith("https://") },
    )

internal fun List<ArticleResponse>.toPage(pageSize: Int): FeedPageDomain {
    val items = take(pageSize).map { it.toDomain() }
    val next = items.lastOrNull()?.takeIf { size > pageSize }
    return FeedPageDomain(
        items = items,
        nextCursor = next?.let { FeedCursorDomain(publishedAt = it.publishedAt, id = it.id) },
    )
}
