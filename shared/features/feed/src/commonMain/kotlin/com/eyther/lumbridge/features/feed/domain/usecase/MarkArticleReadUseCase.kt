package com.eyther.lumbridge.features.feed.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource

class MarkArticleReadUseCase(
    private val feedDataSource: FeedDataSource,
) {

    suspend operator fun invoke(id: String, read: Boolean): AppResult<Unit> = feedDataSource.markArticleRead(id, read)
}
