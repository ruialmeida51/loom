package com.eyther.lumbridge.features.feed.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedDataSource
import com.eyther.lumbridge.features.feed.data.network.validation.parseFeedUrl
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class FeedUrlValidationTest {

    @Test
    fun canonicalizesAnAbsoluteHttpsAddressWithoutChangingItsPathOrQuery() {
        assertEquals(
            "https://feeds.example.com/News/RSS.xml?language=pt",
            parseFeedUrl("  HTTPS://Feeds.Example.com/News/RSS.xml?language=pt#section  "),
        )
    }

    @Test
    fun malformedAddressesFailBeforeAnUnconfiguredClientCanBeUsed() = runTest {
        val source = CommonFeedDataSource(
            access = SupabaseAccess(client = null),
            context = HouseholdContextDomain(
                id = "household",
                userId = "member",
                name = "Home",
                currency = "EUR",
                timeZone = "UTC",
            ),
        )
        val invalid = listOf(
            "",
            "https://",
            "https:///missing-host",
            "feeds.example.com/rss",
            "http://feeds.example.com/rss",
            "https://user:password@feeds.example.com/rss",
            "https://feeds.example.com:/rss",
            "https://feeds.example.com:0/rss",
            "https://feeds.example.com:70000/rss",
            "https://bad_host.example/rss",
            "https://feeds.example.com/a b",
            "https://feeds.example.com/\nfeed",
            "https://feeds.example.com/" + "x".repeat(2048),
        )

        invalid.forEach { address ->
            val result = source.followFeed(url = address)
            assertIs<FeedError.InvalidUrl>(assertIs<AppResult.Failure>(result).error, address)
        }
    }
}
