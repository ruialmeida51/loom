package com.eyther.lumbridge.features.shopping.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.shopping.data.network.datasource.CommonShoppingDataSource
import com.eyther.lumbridge.features.shopping.domain.errors.ShoppingError
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.usecase.SaveShoppingItemUseCase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class CommonShoppingDataSourceTest {

    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "alice",
            name = "Our home",
            currency = "EUR",
            timeZone = "Europe/Lisbon",
        )

    @Test
    fun completedRemovalPreservesListScopeAndEverySelectedVersion() = runTest {
        var path: String? = null
        var body: JsonObject? = null
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    path = request.url.encodedPath
                    body =
                        Json.decodeFromString<JsonObject>(
                            request.body.toByteArray().decodeToString(),
                        )
                    respond(
                        content = "[]",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val source =
                CommonShoppingDataSource(access = SupabaseAccess(client), context = context)
            source
                .removeCompletedItems(
                    items =
                        listOf(
                            ShoppingItemDomain(
                                "milk",
                                "Milk",
                                "",
                                "Other",
                                true,
                                "food",
                                version = 7,
                            ),
                        ),
                )
                .getOrThrow()
            assertEquals("/rest/v1/rpc/remove_completed_shopping_group", path)
            assertEquals("home", body?.get("p_household_id")?.jsonPrimitive?.content)
            assertEquals("food", body?.get("p_list_id")?.jsonPrimitive?.content)
            assertEquals(7L, (body?.get("p_versions") as JsonObject)["milk"]?.jsonPrimitive?.long)
        } finally {
            client.close()
        }
    }

    @Test
    fun invalidCompletedSelectionUsesItsOwnTypedFailure() = runTest {
        val client = errorClient("P0001", "shopping.message_invalid_completed_selection")
        try {
            val result =
                CommonShoppingDataSource(access = SupabaseAccess(client), context = context)
                    .removeCompletedItems(
                        items =
                            listOf(
                                ShoppingItemDomain(
                                    "milk",
                                    "Milk",
                                    "",
                                    "Other",
                                    true,
                                    "food",
                                    version = 1,
                                ),
                            ),
                    )
            assertIs<ShoppingError.InvalidCompletedSelection>(
                assertIs<AppResult.Failure>(result).error,
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun itemWritesKeepWireFieldNamesVersionsAndHouseholdScope() = runTest {
        var requestPath: String? = null
        var requestBody: JsonObject? = null
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    requestPath = request.url.encodedPath
                    requestBody =
                        Json.decodeFromString<JsonObject>(
                            request.body.toByteArray().decodeToString(),
                        )
                    respond(
                        content =
                            """{"id":"milk","name":"Milk","quantity":"2","category":"Pantry","checked":false,"list_id":"food","version":4,"removed":false,"favorite":true}""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }

        try {
            val source =
                CommonShoppingDataSource(access = SupabaseAccess(client), context = context)
            val item =
                ShoppingItemDomain(
                    id = "milk",
                    name = "Milk",
                    quantity = "2",
                    category = "Pantry",
                    checked = false,
                    listId = "food",
                    version = 3,
                    favorite = true,
                )
            val saved = SaveShoppingItemUseCase(source)(item).getOrThrow()

            assertEquals("/rest/v1/rpc/save_shopping_item", requestPath)
            assertEquals("home", requestBody?.get("p_household_id")?.jsonPrimitive?.content)
            assertEquals("food", requestBody?.get("p_list_id")?.jsonPrimitive?.content)
            assertEquals(3L, requestBody?.get("p_version")?.jsonPrimitive?.long)
            assertEquals(item.copy(version = 4), saved)
        } finally {
            client.close()
        }
    }

    @Test
    fun optimisticConcurrencyIsTypedBeforeItReachesTheUseCase() = runTest {
        val client =
            errorClient(
                "P0001",
                "shopping.message_this_item_changed_refresh_before_undoing_or_editing_it",
            )

        try {
            val source =
                CommonShoppingDataSource(access = SupabaseAccess(client), context = context)
            val item = ShoppingItemDomain("milk", "Milk", "", "Pantry", false, "food", version = 1)
            val result = SaveShoppingItemUseCase(source)(item)
            val error =
                assertIs<ShoppingError.ItemChanged>(assertIs<AppResult.Failure>(result).error)

            assertIs<PostgrestRestException>(error.cause)
        } finally {
            client.close()
        }
    }

    @Test
    fun uniquenessIsRecognizedOnlyForTheExpectedConstraintAndSqlState() = runTest {
        val client =
            errorClient(
                "23505",
                "duplicate key value violates unique constraint \"shopping_lists_active_name_idx\"",
            )

        try {
            val result =
                CommonShoppingDataSource(SupabaseAccess(client), context)
                    .saveList(ShoppingListDomain("food", "Groceries"))

            assertIs<ShoppingError.ListNameAlreadyExists>(assertIs<AppResult.Failure>(result).error)
        } finally {
            client.close()
        }

        val unrelated =
            errorClient(
                "23505",
                "duplicate key value violates unique constraint \"shopping_lists_pkey\"",
            )
        try {
            val result =
                CommonShoppingDataSource(SupabaseAccess(unrelated), context)
                    .saveList(ShoppingListDomain("food", "Groceries"))

            assertIs<CommonError.Unknown>(assertIs<AppResult.Failure>(result).error)
        } finally {
            unrelated.close()
        }
    }

    @Test
    fun partialOrUnrelatedServerMessagesDoNotMatchDomainErrors() = runTest {
        for ((code, message) in
            listOf(
                "23514" to
                    "shopping.message_this_item_changed_refresh_before_undoing_or_editing_it",
                "P0001" to
                    "prefix shopping.message_this_item_changed_refresh_before_undoing_or_editing_it suffix",
            )) {
            val client = errorClient(code, message)
            try {
                val result =
                    CommonShoppingDataSource(SupabaseAccess(client), context)
                        .saveList(ShoppingListDomain("food", "Groceries"))

                assertIs<CommonError.Unknown>(assertIs<AppResult.Failure>(result).error)
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun streamFailuresAreValuesContainingOnlyTypedErrors() = runTest {
        val client = errorClient("42501", "permission denied for table shopping_lists")

        try {
            client.auth.awaitInitialization()
            client.auth.importSession(
                UserSession(
                    accessToken = "test-token",
                    refreshToken = "test-refresh",
                    expiresIn = 3600,
                    tokenType = "Bearer",
                ),
                autoRefresh = false,
            )
            val result = CommonShoppingDataSource(SupabaseAccess(client), context).stream().first()

            assertIs<CommonError.AccessDenied>(assertIs<AppResult.Failure>(result).error)
        } finally {
            client.close()
        }
    }

    private fun errorClient(code: String, message: String) =
        createSupabaseClient("https://loom.test", "test-key") {
            install(Auth) {
                autoLoadFromStorage = false
                autoSaveToStorage = false
            }
            install(Postgrest)
            httpEngine = MockEngine {
                val body = buildJsonObject {
                    put("code", code)
                    put("message", message)
                    put("details", "")
                    put("hint", "")
                }
                respond(
                    content = body.toString(),
                    status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }
}
