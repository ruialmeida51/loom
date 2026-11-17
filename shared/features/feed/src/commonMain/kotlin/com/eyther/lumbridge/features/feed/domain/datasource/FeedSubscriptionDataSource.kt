package com.eyther.lumbridge.features.feed.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import kotlinx.coroutines.flow.Flow

interface FeedSubscriptionDataSource {
    fun stream(): Flow<AppResult<List<FeedSubscriptionDomain>>>

    suspend fun refresh(id: String): AppResult<Unit>

    suspend fun update(subscription: FeedSubscriptionDomain, url: String): AppResult<String>

    suspend fun remove(id: String): AppResult<Unit>
}
