package com.eyther.lumbridge.features.feed.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.feed.data.network.datasource.CommonFeedDataSource
import com.eyther.lumbridge.features.feed.data.network.model.response.ArticleResponse
import com.eyther.lumbridge.features.feed.domain.model.FeedCursorDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedPageDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedQueryDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CommonFeedPageDataSourceTest {
    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "Europe/Lisbon",
        )

    private fun id(number: Int): String =
        "00000000-0000-4000-8000-${number.toString().padStart(12, '0')}"

    private fun response(number: Int): ArticleResponse =
        ArticleResponse(
            id = id(number),
            subscriptionId = id(9011),
            title = "Story $number",
            summary = "Summary",
            url = "https://example.test/$number",
            source = "BBC World",
            publishedAt = "2026-11-17T12:00:00Z",
            read = false,
        )

    @Test
    fun theFirstRequestIsAuthenticatedScopedOrderedAndBounded() = runTest {
        val client =
            client(
                MockEngine { request ->
                    assertEquals("/rest/v1/feed_articles_with_state", request.url.encodedPath)
                    assertEquals("eq.home", request.url.parameters["household_id"])
                    assertEquals("21", request.url.parameters["limit"])
                    assertEquals(
                        "Bearer page-test-token",
                        request.headers[HttpHeaders.Authorization],
                    )
                    assertEquals(
                        "published_at.desc.nullslast,id.desc.nullslast",
                        request.url.parameters["order"],
                    )
                    assertFalse(request.url.parameters.contains("offset"))
                    respond(
                        Json.encodeToString((21 downTo 1).map(::response)),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            )
        try {
            authenticate(client)
            val result =
                assertIs<AppResult.Success<FeedPageDomain>>(
                        CommonFeedDataSource(access = SupabaseAccess(client), context = context)
                            .getPage(FeedQueryDomain(), after = null),
                    )
                    .value
            assertEquals(20, result.items.size)
            assertEquals(id(2), assertNotNull(result.nextCursor).id)
            assertEquals(Instant.parse("2026-11-17T12:00:00Z"), result.items.first().publishedAt)
        } finally {
            client.close()
        }
    }

    @Test
    fun selectedSourcesUnreadAndBothCursorFieldsReachPostgrest() = runTest {
        val client =
            client(
                MockEngine { request ->
                    assertEquals("eq.false", request.url.parameters["is_read"])
                    val sources = assertNotNull(request.url.parameters["subscription_id"])
                    assertTrue(sources.startsWith("in.("))
                    assertTrue(sources.contains(id(9011)) && sources.contains(id(9012)))
                    val cursor = assertNotNull(request.url.parameters["or"])
                    assertEquals(
                        "(published_at.lt.\"2026-11-17T12:00:00Z\",and(published_at.eq.\"2026-11-17T12:00:00Z\",id.lt.${id(30)}))",
                        cursor,
                    )
                    respond("[]", headers = headersOf(HttpHeaders.ContentType, "application/json"))
                },
            )
        try {
            authenticate(client)
            val result =
                CommonFeedDataSource(access = SupabaseAccess(client), context = context)
                    .getPage(
                        query =
                            FeedQueryDomain(
                                subscriptionIds = setOf(id(9011), id(9012)),
                                unreadOnly = true,
                            ),
                        after =
                            FeedCursorDomain(
                                publishedAt = Instant.parse("2026-11-17T12:00:00Z"),
                                id = id(30),
                            ),
                    )
            assertIs<AppResult.Success<*>>(result)
        } finally {
            client.close()
        }
    }

    @Test
    fun aPreviewRequestUsesItsSmallLimitAndAnExactlyFullFinalPageHasNoCursor() = runTest {
        val client =
            client(
                MockEngine { request ->
                    assertEquals("4", request.url.parameters["limit"])
                    respond(
                        Json.encodeToString((3 downTo 1).map(::response)),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            )
        try {
            authenticate(client)
            val result =
                CommonFeedDataSource(access = SupabaseAccess(client), context = context)
                    .getPage(FeedQueryDomain(pageSize = 3), after = null)
            val page = assertIs<AppResult.Success<FeedPageDomain>>(result).value
            assertEquals(3, page.items.size)
            assertEquals(null, page.nextCursor)
        } finally {
            client.close()
        }
    }

    @Test
    fun permissionDenialIsTypedAndNeverEntersARetryLoop() = runTest {
        var requests = 0
        val client =
            client(
                MockEngine {
                    requests++
                    respond(
                        """{"code":"42501","message":"opaque permission refusal"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            )
        try {
            authenticate(client)
            val result =
                CommonFeedDataSource(access = SupabaseAccess(client), context = context)
                    .getPage(FeedQueryDomain(), after = null)
            assertIs<CommonError.AccessDenied>(assertIs<AppResult.Failure>(result).error)
            assertEquals(1, requests)
        } finally {
            client.close()
        }
    }

    private fun client(engine: MockEngine): SupabaseClient =
        createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
            install(Auth) {
                autoLoadFromStorage = false
                autoSaveToStorage = false
            }
            install(Postgrest)
            httpEngine = engine
        }

    private suspend fun authenticate(client: SupabaseClient) {
        client.auth.awaitInitialization()
        client.auth.importSession(
            UserSession(
                accessToken = "page-test-token",
                refreshToken = "test-refresh",
                expiresIn = 3600,
                tokenType = "Bearer",
            ),
            autoRefresh = false,
        )
    }
}
