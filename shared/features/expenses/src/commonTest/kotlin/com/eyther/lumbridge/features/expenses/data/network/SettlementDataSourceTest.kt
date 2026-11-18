package com.eyther.lumbridge.features.expenses.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SettlementDataSourceTest {
    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "UTC",
        )
    private val payment =
        SettlementDomain(
            id = "payment",
            from = "you",
            to = "alex",
            amountMinor = 1250,
            occurredOn = "2026-11-18",
            accountId = "trip",
            createdBy = "you",
            version = 3,
        )

    @Test
    fun editsAndDeletionSendTheOriginalVersionAndExactHouseholdAccountScope() = runTest {
        val calls = mutableListOf<String>()
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    calls += request.url.encodedPath
                    val body =
                        Json.parseToJsonElement(request.body.toByteArray().decodeToString())
                            .jsonObject
                    assertEquals("home", body.getValue("p_household_id").jsonPrimitive.content)
                    assertEquals("trip", body.getValue("p_account_id").jsonPrimitive.content)
                    assertEquals("payment", body.getValue("p_id").jsonPrimitive.content)
                    assertEquals("3", body.getValue("p_version").jsonPrimitive.content)
                    if (request.url.encodedPath.endsWith("save_account_settlement")) {
                        assertEquals("1250", body.getValue("p_amount_minor").jsonPrimitive.content)
                        respond(
                            """{"id":"payment","account_id":"trip","from_user":"you","to_user":"alex","amount_minor":1250,"occurred_on":"2026-11-18","created_by":"you","version":4}""",
                            HttpStatusCode.OK,
                            headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    } else respond("", HttpStatusCode.NoContent)
                }
            }
        try {
            val source = CommonExpenseDataSource(SupabaseAccess(client), context)
            val saved = source.saveSettlement(payment).getOrThrow()
            assertEquals(4, saved.version)
            assertEquals("you", saved.createdBy)
            source.removeSettlement(payment).getOrThrow()
            assertEquals(
                listOf(
                    "/rest/v1/rpc/save_account_settlement",
                    "/rest/v1/rpc/delete_account_settlement",
                ),
                calls,
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun serverConcurrencyAndPermissionRefusalsBecomeTypedPaymentErrors() = runTest {
        for (code in listOf("expenses.payment_changed", "expenses.payment_permission_denied")) {
            val client =
                createSupabaseClient("https://loom.test", "test-key") {
                    install(Postgrest)
                    httpEngine = MockEngine {
                        respond(
                            """{"code":"P0001","message":"$code","details":null,"hint":null}""",
                            HttpStatusCode.BadRequest,
                            headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                }
            try {
                val result =
                    CommonExpenseDataSource(SupabaseAccess(client), context)
                        .removeSettlement(payment)
                val error = assertIs<AppResult.Failure>(result).error
                if (code.endsWith("changed")) assertIs<SettlementError.Changed>(error)
                else assertIs<SettlementError.PermissionDenied>(error)
            } finally {
                client.close()
            }
        }
    }
}
