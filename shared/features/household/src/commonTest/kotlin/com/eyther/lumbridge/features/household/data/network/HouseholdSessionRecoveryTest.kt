package com.eyther.lumbridge.features.household.data.network

import com.eyther.lumbridge.core.data.network.PictureStorage
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import com.eyther.lumbridge.features.household.data.network.datasource.CommonHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@OptIn(SupabaseInternal::class)
class HouseholdSessionRecoveryTest {
    @Test
    fun openingMembersDuringResumeWaitsForTheRecoveredUserInsteadOfThrowing() = runTest {
        val profileUsers = mutableListOf<String>()
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSaveToStorage = false
                    autoSetupPlatform = false
                }
                install(Postgrest) { requireValidSession = true }
                httpEngine = MockEngine { request ->
                    assertEquals("Bearer resumed-token", request.headers[HttpHeaders.Authorization])
                    respond(
                        """[{"user_id":"member","display_name":"Alex","role":"owner"}]""",
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        try {
            client.auth.setSessionStatus(SessionStatus.Initializing)
            val dataSource =
                CommonHouseholdDataSource(
                    access = SupabaseAccess(client = client),
                    profileDataSource = profiles(requestedUsers = profileUsers),
                    pictures = PictureStorage(access = SupabaseAccess(client = client)),
                )
            // This previously threw before any flow could be collected or catch attached.
            val members = dataSource.streamMembers(householdId = "home")
            withContext(Dispatchers.Default) {
                val result = async { members.first() }
                assertFalse(result.isCompleted)
                assertEquals(emptyList(), profileUsers)
                client.auth.setSessionStatus(
                    SessionStatus.Authenticated(
                        UserSession(
                            accessToken = "resumed-token",
                            refreshToken = "rotated-refresh",
                            expiresIn = 3600,
                            tokenType = "Bearer",
                            user = UserInfo(id = "member", aud = "authenticated"),
                        ),
                    ),
                )
                val loaded =
                    assertIs<AppResult.Success<List<MemberDomain>>>(
                        withTimeout(3000) { result.await() },
                    )
                assertEquals("Alex", loaded.value.single().name)
                assertEquals(listOf("member"), profileUsers)
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun aConfirmedSignOutIsATypedStreamFailureEvenWhenTheScreenIsBeingCreated() = runTest {
        val requestedUsers = mutableListOf<String>()
        val client =
            createSupabaseClient("https://loom.test", "test-key") {
                install(Auth) {
                    autoLoadFromStorage = false
                    autoSetupPlatform = false
                }
                httpEngine = MockEngine {
                    error("A signed-out stream cannot issue a household request")
                }
            }
        try {
            client.auth.setSessionStatus(SessionStatus.NotAuthenticated(isSignOut = true))
            val members =
                CommonHouseholdDataSource(
                        access = SupabaseAccess(client = client),
                        profileDataSource = profiles(requestedUsers = requestedUsers),
                        pictures = PictureStorage(access = SupabaseAccess(client = client)),
                    )
                    .streamMembers(householdId = "home")
            val result = withContext(Dispatchers.Default) { withTimeout(3000) { members.first() } }
            assertIs<CommonError.NotAuthenticated>(assertIs<AppResult.Failure>(result).error)
            assertEquals(emptyList(), requestedUsers)
        } finally {
            client.close()
        }
    }

    private fun profiles(requestedUsers: MutableList<String>): ProfileDataSource =
        object : ProfileDataSource {
            override fun stream(userId: String): Flow<AppResult<ProfileDomain>> {
                requestedUsers += userId
                return flowOf(AppResult.Success(ProfileDomain(id = userId, nickname = "Alex")))
            }

            override suspend fun updateNickname(
                userId: String,
                nickname: String,
            ): AppResult<ProfileDomain> = error("Nickname writes are outside this regression")
        }
}
