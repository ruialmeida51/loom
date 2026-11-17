package com.eyther.lumbridge.app.feed.data

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.app.demo.data.DemoFeedDataSource
import com.eyther.lumbridge.app.demo.data.DemoFeedSubscriptionDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.features.feed.data.local.CommonFeedCatalogDataSource
import com.eyther.lumbridge.features.feed.domain.usecase.FollowFeedUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedPageUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetFeedSubscriptionsStreamUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.GetStarterFeedsUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.MarkArticleReadUseCase
import com.eyther.lumbridge.features.feed.domain.usecase.RefreshFeedsUseCase
import com.eyther.lumbridge.features.feed.presentation.viewmodel.FeedScreenViewModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

internal class FeedReaderFixture {
    private val viewModels = ViewModelStore()
    private val store =
        DemoHouseholdStore(DemoHouseholdStore.households.first()).also { household ->
            val templates =
                listOf(
                    "Small changes making shared homes more efficient",
                    "How cities are making room for quieter neighbourhoods",
                    "Fresh ideas for your everyday cooking",
                    "A good weekend starts closer to home",
                    "Planning a little breathing room into the month",
                )
            val original = household.articles.value.first()
            household.articles.value =
                (1..45).map { number ->
                    val weekend = number % 2 != 0
                    original.copy(
                        id = "story-$number",
                        title = templates[(number - 1) % templates.size],
                        summary = "A few useful ideas for the everyday things we share.",
                        subscriptionId = if (weekend) "weekend" else "table",
                        source = if (weekend) "The weekend edit" else "Around the table",
                        publishedAt = Instant.parse("2026-11-17T12:00:00Z") - number.minutes,
                    )
                }
        }
    private val articles = DemoFeedDataSource(store)
    private val subscriptions = DemoFeedSubscriptionDataSource(store)
    val viewModel =
        FeedScreenViewModel(
                getFeedPageUseCase = GetFeedPageUseCase(feedDataSource = articles),
                getFeedSubscriptionsStreamUseCase =
                    GetFeedSubscriptionsStreamUseCase(feedSubscriptionDataSource = subscriptions),
                getStarterFeedsUseCase =
                    GetStarterFeedsUseCase(feedCatalogDataSource = CommonFeedCatalogDataSource()),
                followFeedUseCase = FollowFeedUseCase(feedDataSource = articles),
                markArticleReadUseCase = MarkArticleReadUseCase(feedDataSource = articles),
                refreshFeedsUseCase =
                    RefreshFeedsUseCase(feedSubscriptionDataSource = subscriptions),
                timeZone = store.context.timeZone,
            )
            .also { viewModels.put("reader", it) }

    fun close() {
        viewModels.clear()
    }
}
