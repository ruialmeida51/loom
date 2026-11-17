package com.eyther.lumbridge.features.feed.domain.model

/** A followed source exists independently of whether its publisher has returned any articles. */
data class FeedSubscriptionDomain(
    val id: String,
    val url: String,
    val publisher: String,
    val lastFetchedAt: String?,
    val status: FeedSubscriptionStatusDomain,
)
