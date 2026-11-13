package com.eyther.lumbridge.features.feed.domain.datasource

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import kotlinx.coroutines.flow.Flow

interface FeedDataSource {
    fun observe(): Flow<List<ArticleDomain>>

    suspend fun follow(url: String): AppResult<Unit>

    suspend fun markRead(id: String, read: Boolean): AppResult<Unit>
}
