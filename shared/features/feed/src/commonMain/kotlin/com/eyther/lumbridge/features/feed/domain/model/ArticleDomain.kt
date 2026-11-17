package com.eyther.lumbridge.features.feed.domain.model

import kotlin.time.Instant

data class ArticleDomain(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: String,
    val publishedAt: Instant,
    val read: Boolean,
    val subscriptionId: String,
)
