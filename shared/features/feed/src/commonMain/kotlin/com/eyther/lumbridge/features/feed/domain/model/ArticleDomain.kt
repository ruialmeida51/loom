package com.eyther.lumbridge.features.feed.domain.model

import kotlin.time.Instant

data class ArticleDomain(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: String,
    val publishedAt: Instant,
    val subscriptionId: String,
    /** The publisher's picture for the story (https), when the feed offers one. */
    val imageUrl: String? = null,
)
