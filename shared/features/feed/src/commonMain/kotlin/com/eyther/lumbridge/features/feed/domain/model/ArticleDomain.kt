package com.eyther.lumbridge.features.feed.domain.model

import com.eyther.lumbridge.core.domain.*

data class ArticleDomain(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: String,
    val publishedAt: String,
    val read: Boolean,
)
