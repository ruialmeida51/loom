package com.eyther.lumbridge.features.expenses.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.network.datasource.CommonExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(SupabaseInternal::class)
class ExpenseReceiptDataSourceTest {
    @Test
    fun attachmentAndRemovalRequestOneObjectAndPreserveTheRemovalVersion() = runTest {
        val versions = mutableListOf<String>()
        val path = "home/account/expense/photo.jpg"
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                install(Storage)
                httpEngine = MockEngine { request ->
                    when {
                        request.url.encodedPath == "/rest/v1/expense_receipts" ->
                            respond(
                                "[]",
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        request.method == HttpMethod.Head -> respond("")
                        request.url.encodedPath == "/rest/v1/rpc/save_expense_receipt" -> {
                            assertEquals(
                                "application/vnd.pgrst.object+json",
                                request.headers[HttpHeaders.Accept],
                            )
                            val body =
                                Json.parseToJsonElement(request.body.toByteArray().decodeToString())
                                    .jsonObject
                            versions += body.getValue("p_version").jsonPrimitive.content
                            val removal = body.getValue("p_object_path") == JsonNull
                            val objectPath = if (removal) "null" else "\"$path\""
                            respond(
                                """{"expense_id":"expense","account_id":"account","object_path":$objectPath,"version":${if (removal) 2 else 1}}""",
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                        else -> error("Unexpected receipt request")
                    }
                }
            }
        try {
            val source =
                CommonExpenseReceiptDataSource(
                    access = SupabaseAccess(client),
                    context =
                        HouseholdContextDomain(
                            id = "home",
                            userId = "alice",
                            name = "Home",
                            currency = "EUR",
                            timeZone = "Europe/Lisbon",
                        ),
                )
            val saved =
                assertIs<AppResult.Success<ExpenseReceiptDomain>>(
                        source.savePhoto(
                            receipt =
                                ExpenseReceiptDomain(expenseId = "expense", accountId = "account"),
                            photo =
                                ReceiptPhotoDomain(
                                    data = ByteString(byteArrayOf(1, 2, 3)),
                                    id = "photo",
                                ),
                        ),
                    )
                    .value
            assertEquals(path, saved.objectPath)
            val removed =
                assertIs<AppResult.Success<ExpenseReceiptDomain>>(
                        source.removePhoto(receipt = saved),
                    )
                    .value
            assertEquals(null, removed.objectPath)
            assertEquals(2L, removed.version)
            assertEquals(listOf("0", "1"), versions)
        } finally {
            client.close()
        }
    }

    @Test
    fun uploadsAreAuthenticatedAndImmutableAndKeepTheOriginalExpectedVersion() = runTest {
        val calls = mutableListOf<String>()
        val client =
            createSupabaseClient("https://loom.test", "test-publishable-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                }
                install(Postgrest) { requireValidSession = true }
                install(Storage) { requireValidSession = true }
                httpEngine = MockEngine { request ->
                    assertEquals(
                        "Bearer test-user-token",
                        request.headers[HttpHeaders.Authorization],
                    )
                    calls += "${request.method.value} ${request.url.encodedPath}"
                    when {
                        request.url.encodedPath == "/rest/v1/expense_receipts" ->
                            respond(
                                """[{"expense_id":"expense","account_id":"account","object_path":"old-photo.jpg","version":8}]""",
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        request.method == HttpMethod.Head ->
                            respond(
                                """{"statusCode":404,"error":"not_found","message":"Not found"}""",
                                HttpStatusCode.NotFound,
                                headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        request.url.encodedPath.startsWith("/storage/v1/object/") -> {
                            assertEquals(HttpMethod.Post, request.method)
                            assertEquals("false", request.headers["x-upsert"])
                            assertEquals("image/jpeg", request.body.contentType?.toString())
                            respond(
                                """{"Key":"expense-receipts/home/account/expense/photo.jpg","Id":"test-object"}""",
                                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                        request.url.encodedPath == "/rest/v1/rpc/save_expense_receipt" -> {
                            val body =
                                Json.parseToJsonElement(request.body.toByteArray().decodeToString())
                                    .jsonObject
                            assertEquals("3", body.getValue("p_version").jsonPrimitive.content)
                            assertEquals(
                                "home/account/expense/photo.jpg",
                                body.getValue("p_object_path").jsonPrimitive.content,
                            )
                            respond(
                                """{"code":"P0001","message":"expenses.receipt_changed","details":null,"hint":null}""",
                                HttpStatusCode.BadRequest,
                                headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        }
                        else -> error("Unexpected receipt request: ${request.url.encodedPath}")
                    }
                }
            }
        try {
            client.auth.awaitInitialization()
            client.auth.setSessionStatus(
                SessionStatus.Authenticated(
                    UserSession(
                        accessToken = "test-user-token",
                        refreshToken = "test-refresh-token",
                        expiresIn = 3600,
                        tokenType = "Bearer",
                    ),
                ),
            )
            val dataSource =
                CommonExpenseReceiptDataSource(
                    access = SupabaseAccess(client),
                    context =
                        HouseholdContextDomain(
                            id = "home",
                            userId = "alice",
                            name = "Home",
                            currency = "EUR",
                            timeZone = "Europe/Lisbon",
                        ),
                )
            val result =
                dataSource.savePhoto(
                    receipt =
                        ExpenseReceiptDomain(
                            expenseId = "expense",
                            accountId = "account",
                            version = 3,
                        ),
                    photo =
                        ReceiptPhotoDomain(data = ByteString(byteArrayOf(1, 2, 3)), id = "photo"),
                )
            assertIs<ReceiptError.ReceiptChanged>(assertIs<AppResult.Failure>(result).error)
            assertEquals(4, calls.size)
            assertTrue(calls.none { it.startsWith("DELETE") })
        } finally {
            client.close()
        }
    }

    @Test
    fun aLostCommitResponseReconcilesTheStablePhotoWithoutAnotherUploadOrWrite() = runTest {
        var calls = 0
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                install(Storage)
                httpEngine = MockEngine { request ->
                    calls++
                    assertEquals("/rest/v1/expense_receipts", request.url.encodedPath)
                    respond(
                        """[{"expense_id":"expense","account_id":"account","object_path":"home/account/expense/photo.jpg","version":4}]""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val dataSource =
                CommonExpenseReceiptDataSource(
                    access = SupabaseAccess(client),
                    context =
                        HouseholdContextDomain(
                            id = "home",
                            userId = "alice",
                            name = "Home",
                            currency = "EUR",
                            timeZone = "Europe/Lisbon",
                        ),
                )
            val result =
                dataSource.savePhoto(
                    receipt =
                        ExpenseReceiptDomain(
                            expenseId = "expense",
                            accountId = "account",
                            version = 3,
                        ),
                    photo =
                        ReceiptPhotoDomain(data = ByteString(byteArrayOf(1, 2, 3)), id = "photo"),
                )
            assertEquals(
                4L,
                assertIs<AppResult.Success<ExpenseReceiptDomain>>(result).value.version,
            )
            assertEquals(1, calls)
        } finally {
            client.close()
        }
    }
}
