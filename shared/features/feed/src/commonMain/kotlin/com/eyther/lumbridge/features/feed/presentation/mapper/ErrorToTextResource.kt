package com.eyther.lumbridge.features.feed.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.error_already_followed
import com.eyther.lumbridge.features.feed.generated.resources.error_invalid_url
import com.eyther.lumbridge.features.feed.generated.resources.error_refresh_failed
import com.eyther.lumbridge.features.feed.generated.resources.feed_changed
import com.eyther.lumbridge.features.feed.generated.resources.feed_not_found
import com.eyther.lumbridge.features.feed.generated.resources.feed_rate_limited

fun AppError.toFeedTextResource(): TextResource =
    when (this) {
        is FeedError.InvalidUrl -> TextResource.Resource(Res.string.error_invalid_url)
        is FeedError.AlreadyFollowed -> TextResource.Resource(Res.string.error_already_followed)
        is FeedError.RefreshFailed -> TextResource.Resource(Res.string.error_refresh_failed)
        is FeedError.RefreshRateLimited -> TextResource.Resource(Res.string.feed_rate_limited)
        is FeedError.SubscriptionChanged -> TextResource.Resource(Res.string.feed_changed)
        is FeedError.SubscriptionNotFound -> TextResource.Resource(Res.string.feed_not_found)
        else -> toTextResource()
    }
