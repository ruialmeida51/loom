package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class FollowFeedUseCase(private val source: FeedDataSource) {
    suspend operator fun invoke(url: String): AppResult<Unit> {
        if (!url.trim().startsWith("https://") || url.length > 2048)
            return AppResult.Failure("Enter a secure RSS or Atom feed URL beginning with https://.")
        return source.follow(url.trim())
    }
}
