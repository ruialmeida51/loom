package com.eyther.lumbridge.features.account.data.network.datasource

import com.eyther.lumbridge.core.data.network.PictureStorage
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.data.network.mapper.toDomain
import com.eyther.lumbridge.features.account.data.network.model.request.SetProfilePictureRequest
import com.eyther.lumbridge.features.account.data.network.model.request.UpdateNicknameRequest
import com.eyther.lumbridge.features.account.data.network.model.response.ProfileResponse
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class CommonProfileDataSource(
    private val access: SupabaseAccess,
    private val pictures: PictureStorage,
) : ProfileDataSource {
    private val changes = MutableStateFlow(0L)

    override fun stream(userId: String): Flow<AppResult<ProfileDomain>> =
        changes
            .flatMapLatest {
                access.watch {
                    access
                        .requireClient()
                        .from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingle<ProfileResponse>()
                        .toDomain()
                }
            }
            .asApiResult(retryTransientFailures = true)
            .distinctUntilChanged()

    override suspend fun updateNickname(
        userId: String,
        nickname: String,
    ): AppResult<ProfileDomain> = safeApiCall {
        val profile =
            access.write { client ->
                client
                    .from("profiles")
                    .update(UpdateNicknameRequest(displayName = nickname)) {
                        filter { eq("id", userId) }
                        select()
                    }
                    .decodeSingle<ProfileResponse>()
                    .toDomain()
            }
        changes.update { oldVersion -> oldVersion + 1L }
        profile
    }

    override suspend fun setPicture(userId: String, bytes: ByteArray?): AppResult<Unit> =
        safeApiCall {
            val path = bytes?.let { pictures.upload(folder = "profiles/$userId", bytes = it) }
            try {
                // The server links the new picture and queues the previous one for deletion.
                access.write { client ->
                    client.postgrest.rpc("set_profile_picture", SetProfilePictureRequest(path = path))
                }
            } catch (error: Exception) {
                if (error !is CancellationException) path?.let { pictures.discard(it) }
                throw error
            }
            changes.update { oldVersion -> oldVersion + 1L }
        }
}
