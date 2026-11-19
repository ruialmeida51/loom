package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class DemoHouseholdManagementDataSource(private val store: DemoHouseholdStore) :
    HouseholdManagementDataSource {
    private val invitation = MutableStateFlow<HouseholdInvitationDomain?>(null)

    override fun stream(): Flow<AppResult<HouseholdManagementDomain>> =
        combine(store.memberUpdates, invitation) { members, invite ->
            AppResult.Success(
                HouseholdManagementDomain(
                    members = members.filter { it.active },
                    invitation = invite,
                ),
            )
        }

    override suspend fun replaceInvitation(
        previous: HouseholdInvitationDomain?,
        requestId: String,
    ): AppResult<HouseholdInvitationDomain> = safeCall {
        requireOwner()
        if (previous?.id != invitation.value?.id || previous?.version != invitation.value?.version)
            throw HouseholdError.InvitationChanged()
        HouseholdInvitationDomain(
                id = newId(),
                version = 1,
                code = "ABC234",
                expiresAt = (Clock.System.now() + 24.hours).toString(),
                reusable = true,
            )
            .also { invitation.value = it }
    }

    override suspend fun revokeInvitation(
        invitation: HouseholdInvitationDomain,
        requestId: String,
    ): AppResult<Unit> = safeCall {
        requireOwner()
        if (this.invitation.value?.id != invitation.id) throw HouseholdError.InvitationChanged()
        this.invitation.value = null
    }

    override suspend fun changeMember(
        member: MemberDomain,
        action: HouseholdMemberActionDomain,
        requestId: String,
    ): AppResult<Unit> = safeCall {
        if (action != HouseholdMemberActionDomain.LEAVE) requireOwner()
        val current =
            store.members.firstOrNull { it.id == member.id } ?: throw HouseholdError.Changed()
        if (!current.active || current.version != member.version) throw HouseholdError.Changed()
        if (
            current.isOwner &&
                action != HouseholdMemberActionDomain.PROMOTE &&
                store.members.count { it.isOwner } <= 1
        )
            throw HouseholdError.LastOwner()
        val changed =
            when (action) {
                HouseholdMemberActionDomain.PROMOTE ->
                    current.copy(role = "owner", version = current.version + 1)
                HouseholdMemberActionDomain.DEMOTE ->
                    current.copy(role = "member", version = current.version + 1)
                HouseholdMemberActionDomain.REMOVE,
                HouseholdMemberActionDomain.LEAVE ->
                    current.copy(active = false, version = current.version + 1)
            }
        store.members = store.members.map { if (it.id == member.id) changed else it }
        store.memberUpdates.value = store.members
        if (!changed.active) invitation.value = null
    }

    override suspend fun deleteHousehold(name: String, requestId: String): AppResult<Unit> =
        safeCall {
            requireOwner()
            if (name != store.context.name) throw HouseholdError.NameConfirmation()
            store.deleted.value = true
            store.members = store.members.map { it.copy(active = false, version = it.version + 1) }
            store.memberUpdates.value = store.members
            invitation.value = null
        }

    private fun requireOwner() {
        if (store.members.none { it.id == store.context.userId && it.isOwner })
            throw HouseholdError.OwnerRequired()
    }
}
