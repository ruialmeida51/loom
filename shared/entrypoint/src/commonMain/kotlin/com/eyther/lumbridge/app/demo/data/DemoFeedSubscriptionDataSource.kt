package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import io.ktor.http.Url
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoFeedSubscriptionDataSource(private val store: DemoHouseholdStore) :
    FeedSubscriptionDataSource {
    override fun stream() = store.feedSubscriptions.map { AppResult.Success(it) }

    override suspend fun refresh(id: String) = safeCall {
        store.feedSubscriptions.update { subscriptions ->
            subscriptions.map {
                if (it.id == id)
                    it.copy(lastFetchedAt = nowIso(), status = FeedSubscriptionStatusDomain.READY)
                else it
            }
        }
        Unit
    }

    override suspend fun update(subscription: FeedSubscriptionDomain, url: String) = safeCall {
        val address = parseFeedUrl(value = url)
        val id = newId()
        store.feedSubscriptions.update { subscriptions ->
            subscriptions.map {
                if (it.id == subscription.id)
                    it.copy(
                        id = id,
                        url = address,
                        publisher = Url(address).host,
                        lastFetchedAt = null,
                        status = FeedSubscriptionStatusDomain.PENDING,
                    )
                else it
            }
        }
        store.articles.update { articles ->
            articles.filterNot { it.source == subscription.publisher }
        }
        id
    }

    override suspend fun remove(id: String) = safeCall {
        val source = store.feedSubscriptions.value.firstOrNull { it.id == id }?.publisher
        store.feedSubscriptions.update { subscriptions -> subscriptions.filterNot { it.id == id } }
        store.articles.update { articles -> articles.filterNot { it.source == source } }
        Unit
    }
}
