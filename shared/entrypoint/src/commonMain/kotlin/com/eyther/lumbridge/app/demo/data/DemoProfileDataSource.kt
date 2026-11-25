package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DemoProfileDataSource(private val stores: Collection<DemoHouseholdStore>) :
    ProfileDataSource {
    override fun stream(userId: String): Flow<AppResult<ProfileDomain>> =
        stores.first().memberUpdates.map { members ->
            val member = members.first { it.id == userId }
            AppResult.Success(
                ProfileDomain(id = member.id, nickname = member.name, avatarPath = member.avatarPath),
            )
        }

    override suspend fun updateNickname(
        userId: String,
        nickname: String,
    ): AppResult<ProfileDomain> = safeCall {
        stores.forEach { store ->
            store.members =
                store.members.map { member ->
                    if (member.id == userId) member.copy(name = nickname) else member
                }
            store.memberUpdates.value = store.members
        }
        ProfileDomain(id = userId, nickname = nickname)
    }

    override suspend fun setPicture(userId: String, bytes: ByteArray?): AppResult<Unit> = safeCall {
        val previous = stores.first().members.firstOrNull { it.id == userId }?.avatarPath
        val path = DemoPictures.replace(folder = "profiles/$userId", bytes = bytes, previous = previous)
        stores.forEach { store ->
            store.members =
                store.members.map { member ->
                    if (member.id == userId) member.copy(avatarPath = path) else member
                }
            store.memberUpdates.value = store.members
        }
    }
}
