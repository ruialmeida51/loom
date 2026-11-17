package com.eyther.lumbridge.features.feed.data.network.mapper

import com.eyther.lumbridge.features.feed.data.network.model.response.FeedSubscriptionResponse
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import io.ktor.http.Url

internal fun FeedSubscriptionResponse.toDomain(): FeedSubscriptionDomain =
    FeedSubscriptionDomain(
        id = id,
        url = url,
        publisher = Url(url).host,
        lastFetchedAt = lastFetchedAt,
        status =
            when {
                lastError == "This publisher is not supported yet" ->
                    FeedSubscriptionStatusDomain.UNSUPPORTED_PUBLISHER
                lastError != null -> FeedSubscriptionStatusDomain.REFRESH_FAILED
                lastFetchedAt == null -> FeedSubscriptionStatusDomain.PENDING
                else -> FeedSubscriptionStatusDomain.READY
            },
    )
