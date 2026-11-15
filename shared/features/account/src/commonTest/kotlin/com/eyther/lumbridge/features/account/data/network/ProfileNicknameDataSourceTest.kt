package com.eyther.lumbridge.features.account.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.data.network.datasource.CommonProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileNicknameDataSourceTest {
    private val userId = "11111111-1111-1111-1111-111111111111"

    @Test
    fun writesOnlyTheCurrentProfilesNicknameAndRequiresTheReturnedRow() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine { request ->
                    assertEquals(HttpMethod.Patch, request.method)
                    assertEquals("/rest/v1/profiles", request.url.encodedPath)
                    assertEquals("eq.$userId", request.url.parameters["id"])
                    assertTrue(
                        request.headers["Prefer"].orEmpty().contains("return=representation"),
                    )
                    val body =
                        Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                    assertEquals(setOf("display_name"), body.keys)
                    assertEquals("José", body.getValue("display_name").jsonPrimitive.content)
                    respond(
                        content = """[{"id":"$userId","display_name":"José"}]""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val source = CommonProfileDataSource(access = SupabaseAccess(client = client))
            assertIs<AppResult.Success<ProfileDomain>>(
                source.updateNickname(userId = userId, nickname = "José"),
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun anUpdateThatMatchedNoVisibleProfileCannotClaimSuccess() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine {
                    respond(
                        content = "[]",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            assertIs<AppResult.Failure>(
                CommonProfileDataSource(access = SupabaseAccess(client = client))
                    .updateNickname(userId = userId, nickname = "Rui"),
            )
        } finally {
            client.close()
        }
    }

    @Test
    fun serverDenialIsATypedDomainFailure() = runTest {
        val client =
            createSupabaseClient(supabaseUrl = "https://loom.test", supabaseKey = "test-key") {
                install(Postgrest)
                httpEngine = MockEngine {
                    respond(
                        content = """{"code":"42501","message":"opaque server text"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            val result =
                CommonProfileDataSource(access = SupabaseAccess(client = client))
                    .updateNickname(userId = userId, nickname = "Rui")
            assertIs<CommonError.AccessDenied>(assertIs<AppResult.Failure>(result).error)
        } finally {
            client.close()
        }
    }
}
