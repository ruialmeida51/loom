package com.eyther.lumbridge.features.feed.domain.model

enum class FeedSubscriptionStatusDomain {
    PENDING,
    READY,
    UNSUPPORTED_PUBLISHER,
    REFRESH_FAILED,
}
