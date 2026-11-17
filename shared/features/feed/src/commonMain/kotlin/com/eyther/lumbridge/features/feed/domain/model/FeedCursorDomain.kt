package com.eyther.lumbridge.features.feed.domain.model

import kotlin.time.Instant

/** Both ordering fields are needed when a publisher gives several stories the same timestamp. */
data class FeedCursorDomain(val publishedAt: Instant, val id: String)
