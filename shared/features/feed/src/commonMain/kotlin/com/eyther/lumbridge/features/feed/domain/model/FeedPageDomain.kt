package com.eyther.lumbridge.features.feed.domain.model

/** The next cursor is absent at the end, including an exactly-full final page. */
data class FeedPageDomain(val items: List<ArticleDomain>, val nextCursor: FeedCursorDomain?)
