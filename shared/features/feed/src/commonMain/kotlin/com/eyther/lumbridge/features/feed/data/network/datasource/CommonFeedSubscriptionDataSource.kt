package com.eyther.lumbridge.features.feed.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.network.mapper.toDomain
import com.eyther.lumbridge.features.feed.data.network.mapper.toFeedError
import com.eyther.lumbridge.features.feed.data.network.model.request.RefreshFeedRequest
import com.eyther.lumbridge.features.feed.data.network.model.request.UpdateFeedSubscriptionRequest
import com.eyther.lumbridge.features.feed.data.network.model.response.FeedSubscriptionResponse
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.datasource.FeedSubscriptionDataSource
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonFeedSubscriptionDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : FeedSubscriptionDataSource {
    override fun stream(): Flow<AppResult<List<FeedSubscriptionDomain>>> =
        access
            .watch {
                access
                    .selectHouseholdRows<FeedSubscriptionResponse>(
                        table = "feed_subscriptions_with_publisher",
                        householdId = context.id,
                    )
                    .map { it.toDomain() }
                    .sortedBy { it.publisher }
            }
            .asApiResult(mapError = Exception::toFeedError, retryTransientFailures = true)

    override suspend fun refresh(id: String): AppResult<Unit> =
        safeApiCall(mapError = Exception::toFeedError) {
            access
                .requireClient()
                .functions
                .invoke(function = "refresh-feed", body = RefreshFeedRequest(subscriptionId = id))
            Unit
        }

    override suspend fun update(
        subscription: FeedSubscriptionDomain,
        url: String,
    ): AppResult<String> =
        safeApiCall(mapError = Exception::toFeedError) {
            val address = parseFeedUrl(value = url)
            if (address == subscription.url) return@safeApiCall subscription.id
            val replacementId = newId()
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        function = "update_feed_subscription",
                        parameters =
                            UpdateFeedSubscriptionRequest(
                                householdId = context.id,
                                id = subscription.id,
                                expectedUrl = subscription.url,
                                newId = replacementId,
                                url = address,
                            ),
                    )
            }
            replacementId
        }

    override suspend fun remove(id: String): AppResult<Unit> =
        safeApiCall(mapError = Exception::toFeedError) {
            access.write { client ->
                client.from("feed_subscriptions").delete {
                    filter {
                        eq("household_id", context.id)
                        eq("id", id)
                    }
                }
            }
            Unit
        }
}
