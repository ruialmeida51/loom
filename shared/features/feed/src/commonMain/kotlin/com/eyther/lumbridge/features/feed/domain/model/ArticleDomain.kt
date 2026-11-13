package com.eyther.lumbridge.features.feed.domain.model

data class ArticleDomain(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: String,
    val publishedAt: String,
    val read: Boolean,
)
