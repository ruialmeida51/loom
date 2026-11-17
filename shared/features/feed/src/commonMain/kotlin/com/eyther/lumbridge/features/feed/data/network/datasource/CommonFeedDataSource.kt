package com.eyther.lumbridge.features.feed.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.network.mapper.toFeedError
import com.eyther.lumbridge.features.feed.data.network.mapper.toPage
import com.eyther.lumbridge.features.feed.data.network.model.request.FollowFeedRequest
import com.eyther.lumbridge.features.feed.data.network.model.request.ReadArticleRequest
import com.eyther.lumbridge.features.feed.data.network.model.request.RefreshFeedRequest
import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class CommonFeedDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : FeedDataSource {

    override fun stream(): Flow<AppResult<List<ArticleDomain>>> {
        return access
            .watch("feed_articles", context.id) {
                selectPage(query = FeedQueryDomain(pageSize = HOME_PREVIEW_SIZE), after = null)
                    .items
            }
            .asApiResult(mapError = Exception::toFeedError, retryTransientFailures = true)
    }

    override suspend fun getPage(
        query: FeedQueryDomain,
        after: FeedCursorDomain?,
    ): AppResult<FeedPageDomain> =
        safeApiCall(mapError = Exception::toFeedError) {
            access.read { selectPage(query = query, after = after) }
        }

    private suspend fun selectPage(
        query: FeedQueryDomain,
        after: FeedCursorDomain?,
    ): FeedPageDomain {
        return access
            .requireClient()
            .from("feed_articles_with_state")
            .select {
                filter {
                    eq("household_id", context.id)
                    if (query.subscriptionIds.isNotEmpty())
                        isIn("subscription_id", query.subscriptionIds.toList())
                    if (query.unreadOnly) eq("is_read", false)
                    if (after != null)
                        or {
                            lt("published_at", after.publishedAt.toString())
                            and {
                                eq("published_at", after.publishedAt.toString())
                                lt("id", after.id)
                            }
                        }
                }
                order(column = "published_at", order = Order.DESCENDING)
                order(column = "id", order = Order.DESCENDING)
                limit(count = (query.pageSize + 1).toLong())
            }
            .decodeList<ArticleResponse>()
            .toPage(pageSize = query.pageSize)
    }

    override suspend fun followFeed(url: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toFeedError,
        ) {
            val address = parseFeedUrl(value = url)
            val id = newId()
            val request =
                FollowFeedRequest(
                    id = id,
                    householdId = context.id,
                    url = address,
                    createdBy = context.userId,
                )

            access.requireClient().from("feed_subscriptions").insert(request)
            try {
                access
                    .requireClient()
                    .functions
                    .invoke(
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
            val request =
                ReadArticleRequest(
                    articleId = id,
                    userId = context.userId,
                    read = read,
                )

            access.requireClient().from("article_reads").upsert(request)
            Unit
        }
    }
}

private const val HOME_PREVIEW_SIZE = 3
