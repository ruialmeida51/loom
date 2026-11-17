package com.eyther.lumbridge.features.tasks.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.network.datasource.CommonTaskDataSource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CommonTaskDataSourceTest {
    private val context =
        HouseholdContextDomain(
            id = "home",
            userId = "you",
            name = "Home",
            currency = "EUR",
            timeZone = "Europe/Lisbon",
        )
    private val task =
        TaskDomain(
            id = "shelf",
            title = "Fix shelf",
            dueAt = null,
            recurrence = TaskRecurrenceDomain.NONE,
            completed = false,
        )

    @Test
    fun undatedInsertExplicitlyOptsOutOfNotificationsAndKeepsHouseholdScope() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("/rest/v1/tasks", request.url.encodedPath)
                    val body =
                        Json.parseToJsonElement(request.body.toByteArray().decodeToString())
                            .jsonArray
                            .single()
                            .jsonObject
                    assertEquals("home", body.getValue("household_id").jsonPrimitive.content)
                    assertEquals("you", body.getValue("created_by").jsonPrimitive.content)
                    assertFalse(body.getValue("notification_enabled").jsonPrimitive.boolean)
                    assertTrue(body["due_at"] == null || body["due_at"] == JsonNull)
                    respond(content = "", status = HttpStatusCode.Created)
                }
            }
        try {
            assertIs<AppResult.Success<Unit>>(
                CommonTaskDataSource(access = SupabaseAccess(client), context = context)
                    .addTask(task),
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun undatedCompletionUsesTheCanonicalRpcWithoutInventingAnExpectedDate() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    assertEquals("/rest/v1/rpc/complete_task", request.url.encodedPath)
                    val body =
                        Json.parseToJsonElement(request.body.toByteArray().decodeToString())
                            .jsonObject
                    assertEquals("shelf", body.getValue("p_task_id").jsonPrimitive.content)
                    assertTrue(body.getValue("p_completed").jsonPrimitive.boolean)
                    assertTrue(
                        body["p_expected_due_at"] == null || body["p_expected_due_at"] == JsonNull,
                    )
                    respond(content = "", status = HttpStatusCode.NoContent)
                }
            }
        try {
            assertIs<AppResult.Success<Unit>>(
                CommonTaskDataSource(access = SupabaseAccess(client), context = context)
                    .completeTask(task),
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun serverTaskDenialBecomesATypedFeatureError() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine {
                    respond(
                        content = """{"code":"P0001","message":"Task access denied."}""",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val result =
                CommonTaskDataSource(access = SupabaseAccess(client), context = context)
                    .completeTask(task)
            assertIs<TaskError.AccessDenied>(assertIs<AppResult.Failure>(result).error)
        } finally {
            client.close()
        }
    }
}
