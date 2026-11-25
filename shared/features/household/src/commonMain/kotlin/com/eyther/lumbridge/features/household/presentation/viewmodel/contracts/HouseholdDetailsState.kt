package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.mapper.toConfirmation
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.model.HouseholdInvitationUi
import com.eyther.lumbridge.features.household.presentation.model.HouseholdManagementActionUi
import com.eyther.lumbridge.features.household.presentation.model.HouseholdMemberUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class HouseholdDetailsState(
    val userId: String,
    val householdName: String,
    val members: List<MemberDomain> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val invitation: HouseholdInvitationDomain? = null,
    val confirmation: HouseholdManagementActionUi? = null,
    val confirmationName: String = "",
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val successMessage: TextResource? = null,
    val exited: Boolean = false,
    val exitExplicit: Boolean = false,
    val picturePath: String? = null,
    val savingPicture: Boolean = false,
) {
    val invitationUi: HouseholdInvitationUi? = invitation?.let(::HouseholdInvitationUi)
    private val management = HouseholdManagementDomain(members = members, invitation = invitation)
    val isOwner: Boolean = management.isOwner(userId = userId)
    val canInvite: Boolean = isOwner && !busy && !loading
    val canChangePicture: Boolean = !busy && !savingPicture
    val canLeave: Boolean = management.canLeave(userId = userId)
    val confirmationContent = confirmation?.toConfirmation()
    private val confirmationAllowed: Boolean =
        when (val action = confirmation) {
            is HouseholdManagementActionUi.MemberChange ->
                if (action.action == HouseholdMemberActionDomain.LEAVE) canLeave else isOwner
            is HouseholdManagementActionUi.InvitationChange,
            is HouseholdManagementActionUi.Delete -> isOwner
            null -> false
        }
    val confirmationError: TextResource? =
        error
            ?: if (confirmation != null && !confirmationAllowed) {
                if (
                    confirmation is HouseholdManagementActionUi.MemberChange &&
                        confirmation.action == HouseholdMemberActionDomain.LEAVE
                )
                    HouseholdError.LastOwner().toHouseholdTextResource()
                else HouseholdError.OwnerRequired().toHouseholdTextResource()
            } else null
    val canConfirm: Boolean =
        !busy &&
            confirmationAllowed &&
            confirmation != null &&
            (confirmation !is HouseholdManagementActionUi.Delete ||
                confirmationName == householdName)
    val displayError: TextResource? = if (confirmation == null) error ?: loadError else loadError
    val memberRows: ImmutableList<HouseholdMemberUi> =
        members
            .filter { it.active }
            .map { member ->
                HouseholdMemberUi(
                    id = member.id,
                    name = member.name,
                    initials =
                        member.name
                            .firstOrNull { it.isLetterOrDigit() }
                            ?.uppercaseChar()
                            ?.toString()
                            .orEmpty(),
                    isOwner = member.isOwner,
                    isCurrentUser = member.id == userId,
                    canManage = management.canManageMember(actorId = userId, memberId = member.id),
                    avatarPath = member.avatarPath,
                )
            }
            .sortedWith(
                compareByDescending<HouseholdMemberUi> { it.isOwner }
                    .thenBy { it.name.lowercase() }
                    .thenBy { it.id },
            )
            .toImmutableList()
}
