package com.eyther.lumbridge.features.feed.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class FeedError(cause: Throwable? = null) : AppError(cause) {

    class InvalidUrl(cause: Throwable? = null) : FeedError(cause)

    class AlreadyFollowed(cause: Throwable? = null) : FeedError(cause)

    class RefreshFailed(cause: Throwable? = null) : FeedError(cause)

    class RefreshRateLimited(cause: Throwable? = null) : FeedError(cause)

    class SubscriptionChanged(cause: Throwable? = null) : FeedError(cause)

    class SubscriptionNotFound(cause: Throwable? = null) : FeedError(cause)
}
