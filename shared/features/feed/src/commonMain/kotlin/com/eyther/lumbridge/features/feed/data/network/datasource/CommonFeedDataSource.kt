package com.eyther.lumbridge.features.feed.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.network.mapper.toDomain
import com.eyther.lumbridge.features.feed.data.network.mapper.toFeedError
import com.eyther.lumbridge.features.feed.data.network.model.request.FollowFeedRequest
import com.eyther.lumbridge.features.feed.data.network.model.request.ReadArticleRequest
import com.eyther.lumbridge.features.feed.data.network.model.request.RefreshFeedRequest
import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class CommonFeedDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : FeedDataSource {

    override fun stream(): Flow<AppResult<List<ArticleDomain>>> {
        return access.watch("feed_articles", context.id) {
            access.selectHouseholdRows<ArticleResponse>("feed_articles_with_state", context.id).map { it.toDomain() }
        }.asApiResult(mapError = Exception::toFeedError)
    }

    override suspend fun followFeed(url: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toFeedError,
        ) {
            val address = parseFeedUrl(value = url)
            val id = newId()
            val request = FollowFeedRequest(
                id = id,
                householdId = context.id,
                url = address,
                createdBy = context.userId,
            )

            access.requireClient().from("feed_subscriptions").insert(request)
            try {
                access.requireClient().functions.invoke(
                    function = "refresh-feed",
                    body = RefreshFeedRequest(subscriptionId = id),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                throw FeedError.RefreshFailed(cause = error)
            }
        }
    }

    override suspend fun markArticleRead(id: String, read: Boolean): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toFeedError,
        ) {
            val request = ReadArticleRequest(
                articleId = id,
                userId = context.userId,
                read = read,
            )

            access.requireClient().from("article_reads").upsert(request)
            Unit
        }
    }
}
