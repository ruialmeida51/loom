package com.eyther.lumbridge.features.expenses.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

/**
 * Ledger reads wait for an authenticated session, and the SDK's Android session handling does
 * not start in host tests, so these run on the JVM target (CI's jvmTest).
 */
@OptIn(SupabaseInternal::class)
class LedgerDataSourceTest {
    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "UTC",
        )

    private suspend fun client(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): SupabaseClient {
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                    sessionManager = MemorySessionManager()
                    codeVerifierCache = MemoryCodeVerifierCache()
                }
                install(Postgrest)
                httpEngine = MockEngine(handler)
            }
        client.auth.awaitInitialization()
        client.auth.setSessionStatus(
            SessionStatus.Authenticated(
                UserSession(
                    accessToken = "access",
                    refreshToken = "refresh",
                    expiresIn = 3600,
                    tokenType = "Bearer",
                    expiresAt = Clock.System.now() + 1.hours,
                ),
            ),
        )
        return client
    }

    private fun MockRequestHandleScope.json(body: String): HttpResponseData =
        respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun expenseJson(id: String, createdAt: String): String =
        """{"id":"$id","account_id":"trip","version":1,"created_by":"you","split_method":"equal",""" +
            """"split_values":{},"title":"$id","amount_minor":100,"paid_by":"you","category":"Food",""" +
            """"occurred_on":"2026-11-20","shares":{"alex":100},"recurring":false,"created_at":"$createdAt"}"""

    private fun settlementJson(id: String, createdAt: String): String =
        """{"id":"$id","account_id":"trip","from_user":"alex","to_user":"you","amount_minor":100,""" +
            """"occurred_on":"2026-11-20","created_by":"alex","version":1,"created_at":"$createdAt"}"""

    @Test
    fun aPageAsksBothTablesForOneMoreThanItShowsAfterTheSameCursor() = runTest {
        val queries = mutableMapOf<String, Map<String, String?>>()
        val client = client { request ->
            val path = request.url.encodedPath
            queries[path] = request.url.parameters.names().associateWith { request.url.parameters[it] }
            when (path) {
                "/rest/v1/account_expenses_with_shares" ->
                    json(
                        "[" +
                            expenseJson("e2", "2026-11-26T11:00:00+00:00") + "," +
                            expenseJson("e3", "2026-11-26T09:00:00.25+00:00") +
                            "]",
                    )
                else ->
                    json(
                        "[" +
                            settlementJson("s1", "2026-11-26T10:00:00.5+00:00") + "," +
                            settlementJson("s4", "2026-11-26T08:00:00+00:00") +
                            "]",
                    )
            }
        }
        try {
            val source = CommonExpenseDataSource(access = SupabaseAccess(client), context = context)
            val cursor =
                LedgerCursorDomain(createdAt = Instant.parse("2026-11-26T12:00:00.123456Z"), id = "e1")
            val page = source.getLedgerPage(accountId = "trip", after = cursor, limit = 2).getOrThrow()
            assertEquals(listOf("e2", "s1"), page.records.map { it.id })
            assertEquals(page.records.last().cursor, page.nextCursor)
            for (path in listOf("/rest/v1/account_expenses_with_shares", "/rest/v1/settlements")) {
                val query = queries.getValue(path)
                assertEquals("eq.home", query["household_id"])
                assertEquals("eq.trip", query["account_id"])
                assertEquals("created_at.desc.nullslast,id.asc.nullslast", query["order"])
                assertEquals("3", query["limit"])
                assertEquals(
                    "(created_at.lt.\"2026-11-26T12:00:00.123456Z\"," +
                        "and(created_at.eq.\"2026-11-26T12:00:00.123456Z\",id.gt.e1))",
                    query["or"],
                )
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun balancesAreReadFromTheServerForTheExactAccount() = runTest {
        val client = client { request ->
            assertEquals("/rest/v1/account_balances", request.url.encodedPath)
            assertEquals("eq.home", request.url.parameters["household_id"])
            assertEquals("eq.trip", request.url.parameters["account_id"])
            json("""[{"user_id":"you","balance_minor":2500},{"user_id":"alex","balance_minor":-2500}]""")
        }
        try {
            val source = CommonExpenseDataSource(access = SupabaseAccess(client), context = context)
            val balances = source.streamBalances(accountId = "trip").first().getOrThrow()
            assertEquals(mapOf("alex" to -2500L, "you" to 2500L), balances)
            assertEquals(listOf("alex", "you"), balances.keys.toList(), "A stable order")
        } finally {
            client.close()
        }
    }

    @Test
    fun fullHistoryReadsEveryChunkPastTheResponseCap() = runTest {
        val starts = mutableListOf<String?>()
        val client = client { request ->
            val afterId = request.url.parameters["id"]
            starts += afterId
            assertEquals("id.asc.nullslast", request.url.parameters["order"], "A stable order")
            assertEquals("500", request.url.parameters["limit"])
            val from = afterId?.removePrefix("gt.s")?.toInt()?.plus(1) ?: 0
            val rows = if (from < 1000) 500 else 3
            json(
                (from until from + rows).joinToString(prefix = "[", postfix = "]") {
                    settlementJson("s${it.toString().padStart(4, '0')}", "2026-11-26T08:00:00+00:00")
                },
            )
        }
        try {
            val source = CommonExpenseDataSource(access = SupabaseAccess(client), context = context)
            val settlements = source.streamSettlements(accountId = "trip").first().getOrThrow()
            assertEquals(1003, settlements.size)
            assertEquals(1003, settlements.map { it.id }.toSet().size)
            assertEquals(listOf(null, "gt.s0499", "gt.s0999"), starts)
        } finally {
            client.close()
        }
    }
}
