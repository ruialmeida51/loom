package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class FollowFeedUseCase(private val source: FeedDataSource) {
    suspend operator fun invoke(url: String): AppResult<Unit> {
        if (!url.trim().startsWith("https://") || url.length > 2048)
            return AppResult.Failure("feed.message_enter_a_secure_rss_or_atom_feed_url_beginning")
        return source.follow(url.trim())
    }
}
