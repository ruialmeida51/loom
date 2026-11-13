package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class MarkArticleReadUseCase(private val source: FeedDataSource) {
    suspend operator fun invoke(id: String, read: Boolean) = source.markRead(id, read)
}
